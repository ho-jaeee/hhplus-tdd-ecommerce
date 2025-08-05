package kr.hhplus.be.server.point.domain.service;


import kr.hhplus.be.server.point.domain.model.UserPointJPA;
import kr.hhplus.be.server.point.domain.repository.UserPointRepository;
import org.springframework.stereotype.Service;

@Service
public class PointQueryService {

    private final UserPointRepository userPointRepo;

    public PointQueryService(UserPointRepository userPointRepo) {
        this.userPointRepo = userPointRepo;
    }

    public UserPointJPA GetPoint(long userId){

        return userPointRepo.findById(userId);

    }
}
