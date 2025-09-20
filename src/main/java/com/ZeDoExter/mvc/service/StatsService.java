package com.ZeDoExter.mvc.service;

import com.ZeDoExter.mvc.model.PledgeStatus;
import com.ZeDoExter.mvc.repository.PledgeRepository;
import org.springframework.stereotype.Service;


@Service
public class StatsService {
    private final PledgeRepository pledgeRepository;

    public StatsService(PledgeRepository pledgeRepository) {
        this.pledgeRepository = pledgeRepository;
    }

    public long countAccepted() {
        return pledgeRepository.findAll().stream().filter(p -> p.getStatus() == PledgeStatus.ACCEPTED).count();
    }

    public long countRejected() {
        return pledgeRepository.findAll().stream().filter(p -> p.getStatus() == PledgeStatus.REJECTED).count();
    }
}

