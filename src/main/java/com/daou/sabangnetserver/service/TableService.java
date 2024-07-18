package com.daou.sabangnetserver.service;

import com.daou.sabangnetserver.dto.table.TableOrdersBaseDto;
import com.daou.sabangnetserver.dto.table.TableOrdersDetailDto;
import com.daou.sabangnetserver.model.OrdersBase;
import com.daou.sabangnetserver.model.OrdersDetail;
import com.daou.sabangnetserver.repository.OrdersBaseRepository;
import com.daou.sabangnetserver.repository.OrdersDetailRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import java.util.ArrayList;
import java.util.Objects;

@Slf4j
@Service
public class TableService {

    // 주문조회 데이터 저장을 위한 ordersBaseRepository DI
    @Autowired
    private OrdersBaseRepository ordersBaseRepository;

    @Autowired
    private OrdersDetailRepository ordersDetailRepository;

    // 특정 페이지에 따라 페이지네이션을 한 데이터 반환
    public List<TableOrdersBaseDto> getPagenation(int page) {

        // 페이지당 데이터 수
        int pageSize = 10;

        // 페이지 번호와 페이지 크기(한 페이지에 보여줄 항목 수)를 인자로 받아서 해당 페이지의 데이터를 가져와 PageRequest 객체를 생성
        // 페이지 번호(page)는 0부터 시작하며, 페이지 크기(pageSize)는 한 페이지에 표시할 항목 수를 의미
        PageRequest pageRequest = PageRequest.of(page, pageSize);

        // 특정 페이지의 OrdersDetail 데이터를 반환 (detail 객체 안에 ordersBase가 오브젝트로 들어간 형태)
        // 현재 페이지의 데이터 목록 외에도, 전체 페이지 수, 현재 페이지 번호, 총 데이터 개수 등의 정보가 포함되어 있음
        Page<OrdersDetail> ordersDetailPage = getOrdersDetailPage(pageRequest);


        // 앞서 받은 데이터를 기존 테이블 속 데이터 형태로 재매핑
        Map<OrdersBase, List<OrdersDetail>> ordersBaseMap = groupOrdersBase(ordersDetailPage);

        // rowspan,index 포함하여
        return mapToTableOrdersBaseDto(ordersBaseMap, page);
    }



    // 데이터베이스에서 특정 페이지의 주문 상세 정보를 가져옴
    private Page<OrdersDetail> getOrdersDetailPage(PageRequest pageRequest) {
        return ordersDetailRepository.findAllWithOrdersBase(pageRequest);
    }


    // ordersDetail를 ordersBase 기준으로 다시 그룹화
    private Map<OrdersBase, List<OrdersDetail>> groupOrdersBase(Page<OrdersDetail> ordersDetailPage) {
        // ordersDetailPage.getContent()는 현재 페이지의 OrdersDetail 데이터 목록을 가져옴
        // stream()을 통해 데이터들을 순차적으로 처리
        return ordersDetailPage.getContent().stream()
                .collect(Collectors.groupingBy(OrdersDetail::getOrdersBase));
                //  Collectors.groupingBy를 사용하여 스트림의 각 요소를 OrdersBase 기준으로 그룹화하겠다는 의미
                // 새롭게 그룹화 한 각 OrdersBase에 대해 collect 메소드를 활용 -> List<OrdersDetail>을 값으로 가지는 Map 생성
    }


     // 그룹화된 주문 데이터를 반환할 DTO 형태로 변환
    private List<TableOrdersBaseDto> mapToTableOrdersBaseDto(Map<OrdersBase, List<OrdersDetail>> ordersBaseMap, int page) {

        // entrySet()은 Map의 메서드로, Map에 있는 모든 키와 값을 Set<Map.Entry<K,V>> 형태로 반환
        // ordersBaseMap.entrySet().stream()은 각 엔트리를 순차적으로 처리할 수 있는 스트림을 반환
        // 순차적으로 처리한 데이터를 매핑해서 List<TableOrdersBaseDto> 형태로 리턴하는 것이 목적
        return ordersBaseMap.entrySet().stream().map(entry -> {

            // 람다 표현식을 통해 entry 객체의 key와 Value를 할당
            OrdersBase ordersBase = entry.getKey(); // key == 현재 주문 정보
            List<OrdersDetail> ordersDetails = entry.getValue(); // value == 현재 주문에 해당하는 주문 상세 정보 리스트

            // 주문 정보를 DTO 형태로 변환
            TableOrdersBaseDto ordersBaseDto = new TableOrdersBaseDto(ordersBase);

            // 주문 상세 정보를 DTO 형태로 변환
            // ordersDetails -> stream을 통해 각 요소를 순차적으로 처리
            // 기존 OrdersDetail 객체를 새로운 TableOrdersDetailDto 객체로 변환
            // 변환된 TableOrdersDetailDto 객체들을 리스트로 collect
            List<TableOrdersDetailDto> ordersDetailDtos = ordersDetails.stream()
                    .map(TableOrdersDetailDto::new)
                    .collect(Collectors.toList());

            // 앞서 만든 ordersDetailDtos를 ordersBaseDto에 할당
            ordersBaseDto.setOrdersDetail(ordersDetailDtos);

            // 변환된 주문 정보 반환
            return ordersBaseDto;

        }).collect(Collectors.toList()); // ordersBaseDto 객체를 리스트로 collect
    }

    private int[] calRowspanAndIndex(List<OrdersBase> ordersList) {

        if (ordersList == null || ordersList.isEmpty()) {
            return new int[0]; // 데이터가 없으면 빈 배열 반환
        }

        //결과 저장 정수 리스트
        List<Integer> idxSpansArray = new ArrayList<>();
        int currentIndex = 1;
        int currentRowspan = 1;
        Long currentOrdNo = null;

        //주문목록 순회
        for (OrdersBase order : ordersList ) {
            //주문 번호가 다르거나 첫 번째 항목일 경우
            if(currentOrdNo == null || !Objects.equals(currentOrdNo, order.getOrdNo())) {
                if(currentOrdNo != null) {
                    idxSpansArray.add(currentRowspan);
                    idxSpansArray.add(currentIndex);
                    currentIndex++;
                }
                currentOrdNo = order.getOrdNo();
                currentRowspan = 1;
            }
            // 주문 번호가 같다면
            else {
                currentRowspan++;
            }

        }

        // 마지막 주문 항목에 대한 번호 추가
        idxSpansArray.add(currentRowspan);
        idxSpansArray.add(currentIndex);

        return idxSpansArray.stream().mapToInt(Integer::intValue).toArray();
    }




}



