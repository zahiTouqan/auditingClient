package com.i3hub.auditingclient;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@MappedSuperclass
@EntityListeners({AuditingListener.class, AuditingEntityListener.class})
@JsonIgnoreProperties(value = { "createdBy", "createdDate", "lastModifiedDate", "lastModifiedBy" }, allowGetters = true)
public class AbstractEventEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter
    @Setter
    private Long id;

//    @NotNull()
    @Transient
    @JsonProperty(access = Access.READ_ONLY)
    private ObjectNode data;

//    @NotBlank
    @Transient
    @JsonProperty(access = Access.READ_ONLY)
    private String typeOfEntity;

//    @NotNull
    @Transient
    @JsonProperty(access = Access.READ_ONLY)
    private AuditingListener.Operation operation;

//    @NotNull
    @Transient
    @JsonProperty(access = Access.READ_ONLY)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy hh:mm:ss")
    private Date dateOfEvent;

//    @NotBlank
    @Transient
    @JsonProperty(access = Access.READ_ONLY)
    private String serviceName;

//    @NotBlank
    @Transient
    @JsonProperty(access = Access.READ_ONLY)
    private Long userId;

    //@Transient
    @Column(name = "created_by", updatable = false)
    @CreatedBy
    @Getter
//    @Setter
    private Long createdBy;

    @Column(name = "last_updated_by")
    @LastModifiedBy
    @Getter
//    @Setter
    private Long lastModifiedBy;

    //@Transient
    @Column(name = "last_updated_on")
    @LastModifiedDate
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy hh:mm:ss")
    @Getter
//    @Setter
    private Date lastModifiedDate;

    // @Transient
    @Column(name = "created_on", updatable = false)
    @CreatedDate
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy hh:mm:ss")
    @Getter
//    @Setter
    private Date createdDate;
}
