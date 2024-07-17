package com.daou.sabangnetserver.service;

import com.daou.sabangnetserver.model.OrdersBase;
import com.daou.sabangnetserver.repository.OrdersBaseRepository;
import com.daou.sabangnetserver.repository.OrdersDetailRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Slf4j
@Service
public class TableService {

    // 주문조회 데이터 저장을 위한 ordersBaseRepository DI
    @Autowired
    private OrdersBaseRepository ordersBaseRepository;
    @Autowired
    private OrdersDetailRepository ordersDetailRepository;

    // RestTemplate 인스턴스 생성
    private final RestTemplate restTemplate = new RestTemplate();
    /*    public List<OrdersBase> getPagenation(int page){

        // 페이지 번호 받아서 DB에서 분할하는 함수 호출 (리턴 값 -> List<OrdersBase>)
        // 받은 데이터를 rowspan 및 index 계산하는 함수 만들기 (받는 파라미터 위의 함수 결과 / 리턴 값 정수 배열
        // 모든 데이터를 종합해서 JSon 생성
        // http 응답 보내기
 //       return new List<OrdersBase>();
  //  }
*/
}
