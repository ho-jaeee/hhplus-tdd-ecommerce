package kr.hhplus.be.server.point.usecase;

import kr.hhplus.be.server.point.controller.dto.PointResponse;
import kr.hhplus.be.server.point.domain.model.UserPoint;
import kr.hhplus.be.server.point.domain.service.PointQueryService;
import org.springframework.stereotype.Service;


/*
포인트 조회 UseCase는 계층의 명확한 분리를 위해 생성하였음.
서비스와 동일한 로직이여서 별도의 테스트 코드 작성은 안한.
*/

@Service
public class PointQueryUseCase {


    private final PointQueryService pointQueryService;

    public PointQueryUseCase(PointQueryService pointQueryService) {
        this.pointQueryService = pointQueryService;
    }

    //내부로직
    public PointResponse QueryUseCase(long userId) {
        return pointQueryService.GetPoint(userId);
    }

}
