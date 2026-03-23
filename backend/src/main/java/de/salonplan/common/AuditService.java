package de.salonplan.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AuditService {
    private static final Logger log = LoggerFactory.getLogger("AUDIT");

    public void record(String action, String actor, Long targetId) {
        log.info("action={} actor={} targetId={}", action, actor, targetId);
    }
}