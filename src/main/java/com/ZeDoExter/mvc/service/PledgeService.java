package com.ZeDoExter.mvc.service;

import com.ZeDoExter.mvc.model.*;
import com.ZeDoExter.mvc.repository.PledgeRepository;
import com.ZeDoExter.mvc.repository.ProjectRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Service
public class PledgeService {
    private final ProjectRepository projectRepository;
    private final PledgeRepository pledgeRepository;

    public PledgeService(ProjectRepository projectRepository, PledgeRepository pledgeRepository) {
        this.projectRepository = projectRepository;
        this.pledgeRepository = pledgeRepository;
    }

    public static class Result {
        public final boolean accepted;
        public final String message;
        public Result(boolean accepted, String message) { this.accepted = accepted; this.message = message; }
    }

    public Result attemptPledge(String userId, String projectId, double amount, String rewardTierId) {
        Optional<Project> opt = projectRepository.findById(projectId);
        if (opt.isEmpty()) {
            return new Result(false, "Project not found");
        }
        Project project = opt.get();

        Pledge pledge = new Pledge();
        pledge.setId(UUID.randomUUID().toString());
        pledge.setUserId(userId);
        pledge.setProjectId(projectId);
        pledge.setAmount(amount);
        pledge.setTimestamp(Instant.now());
        pledge.setRewardTierId(rewardTierId != null && rewardTierId.isBlank() ? null : rewardTierId);

        // Rules: deadline in future
        LocalDate today = LocalDate.now();
        if (project.getDeadline() == null || !project.getDeadline().isAfter(today)) {
            pledge.setStatus(PledgeStatus.REJECTED);
            pledge.setRejectionReason("Project deadline has passed");
            pledgeRepository.add(pledge);
            return new Result(false, "โครงการหมดเวลาแล้ว");
        }

        // amount > 0
        if (amount <= 0) {
            pledge.setStatus(PledgeStatus.REJECTED);
            pledge.setRejectionReason("Amount must be > 0");
            pledgeRepository.add(pledge);
            return new Result(false, "จำนวนเงินต้องมากกว่า 0 บาท");
        }

        RewardTier selected = null;
        if (pledge.getRewardTierId() != null) {
            for (RewardTier rt : project.getRewardTiers()) {
                if (rt.getId().equals(pledge.getRewardTierId())) {
                    selected = rt; break;
                }
            }
            if (selected == null) {
                pledge.setStatus(PledgeStatus.REJECTED);
                pledge.setRejectionReason("Reward tier not found");
                pledgeRepository.add(pledge);
                return new Result(false, "ไม่พบระดับรางวัลที่เลือก");
            }
            if (amount < selected.getMinAmount()) {
                pledge.setStatus(PledgeStatus.REJECTED);
                pledge.setRejectionReason("Amount below reward minimum");
                pledgeRepository.add(pledge);
                return new Result(false, "จำนวนเงินต่ำกว่ายอดขั้นต่ำของรางวัล");
            }
            if (selected.getRemainingQuota() <= 0) {
                pledge.setStatus(PledgeStatus.REJECTED);
                pledge.setRejectionReason("Reward quota exhausted");
                pledgeRepository.add(pledge);
                return new Result(false, "โควตาของรางวัลนี้หมดแล้ว");
            }
        }

        // Accept
        pledge.setStatus(PledgeStatus.ACCEPTED);
        pledgeRepository.add(pledge);
        return new Result(true, "บันทึกการสนับสนุนสำเร็จ ขอบคุณครับ/ค่ะ");
    }
}

