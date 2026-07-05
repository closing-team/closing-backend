package com.closing.closing.domain.rag.entity;

import com.closing.closing.global.entity.BaseCreatedEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "document_chunks")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DocumentChunk extends BaseCreatedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chunk_id")
    private Long id;

    @Column(nullable = false)
    private String sourceType;

    @Column(nullable = false)
    private String sourceId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;


    @Builder
    public DocumentChunk(String sourceType, String sourceId, String content) {
        this.sourceType = sourceType;
        this.sourceId = sourceId;
        this.content = content;
    }
}