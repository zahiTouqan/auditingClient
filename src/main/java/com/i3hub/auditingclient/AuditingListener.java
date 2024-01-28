package com.i3hub.auditingclient;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.persistence.PostPersist;
import jakarta.persistence.PostRemove;
import jakarta.persistence.PostUpdate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class AuditingListener {

    private final ExecutorService EXECUTOR = Executors.newCachedThreadPool();

    @Autowired
    private TopicProducer topicProducer;

    @Autowired
    private HTTPClientService httpClientService;

    @Value("${auditing.rest:true}")
    private boolean auditingRest;

    @Value("${auditing.restURL}")
    private String auditRestURL;

    @Value("${auditing.serviceName}")
    private String serviceName;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public enum Operation {
        Created,
        Updated,
        Removed
    }

    @PostPersist
    private void afterSaving(Object object) {
        submitToThread(object, Operation.Created);
    }

    @PostRemove
    private void afterRemoving(Object object) {
        submitToThread(object, Operation.Removed);
    }

    @PostUpdate
    private void afterUpdating(Object object) {
        submitToThread(object, Operation.Updated);
    }

    private void submitToThread(Object object, Operation operation) {
        Long userId = getUserId();
        AbstractEventEntity temp = (AbstractEventEntity) object;
        EXECUTOR.submit(() -> {
            AbstractEventEntity abstractEventEntity = loadAsAbstract(object, operation, temp.getId(), userId, temp.getCreatedBy(), temp.getLastModifiedBy(), temp.getCreatedDate(), temp.getLastModifiedDate());
            try {
                send(abstractEventEntity);
            } catch (JSONException | JsonProcessingException | ExecutionException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private Long getUserId() {
        Long userId;
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            userId = getCurrentUserId();
        } else {
            userId = null;
        }
        return userId;
    }

    @SuppressWarnings("unused")
    abstract static class MixIn {
        @JsonIgnore private Long id;
        @JsonIgnore private ObjectNode data;
        @JsonIgnore private String typeOfEntity;
        @JsonIgnore private Operation operation;
        @JsonIgnore private Date dateOfEvent;
        @JsonIgnore private String serviceName;
        @JsonIgnore private String userId;
        @JsonIgnore private String createdBy;
        @JsonIgnore private String lastModifiedBy;
        @JsonIgnore private Date lastModifiedDate;
        @JsonIgnore private Date createdDate;
    }


    private AbstractEventEntity loadAsAbstract(Object object, Operation operation, Long id, Long userId, Long createdBy, Long lastModifiedBy, Date createdOn, Date updatedOn) {
        ObjectMapper testMapper = new ObjectMapper();
        testMapper.addMixIn(AbstractEventEntity.class, MixIn.class);
        ObjectNode json = testMapper.valueToTree(object);
        String typeOfEntity = object.getClass().getSimpleName();
        return new AbstractEventEntity(id, json,
            typeOfEntity, operation, new Date(), serviceName,
            userId, createdBy, lastModifiedBy, updatedOn, createdOn);
    }

    public static Long getCurrentUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof Jwt jwt) {
            return jwt.getClaim("user_id");
        } else {
            return null;
        }
    }

    private void send(AbstractEventEntity object) throws JSONException, JsonProcessingException, ExecutionException, InterruptedException {
        ObjectNode objectNode = objectMapper.valueToTree(object);
        if (auditingRest) {
            httpClientService.makeRequest(objectNode);
        } else {
            topicProducer.send(objectNode.toString());
        }
    }
}
