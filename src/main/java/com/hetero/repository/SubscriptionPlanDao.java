package com.hetero.repository;

import com.hetero.models.SubscriptionPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface SubscriptionPlanDao extends JpaRepository<SubscriptionPlan, Long> {
}
