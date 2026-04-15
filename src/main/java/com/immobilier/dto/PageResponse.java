package com.immobilier.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> {

    /** Contenu de la page courante. */
    private List<T> content;

    /** Nombre total d'éléments toutes pages confondues. */
    private long totalElements;

    /** Nombre total de pages. */
    private int totalPages;

    /** Numéro de la page courante (commence à 0). */
    private int number;

    /** Taille demandée (nb d'éléments par page). */
    private int size;

    /** Indique si c'est la première page. */
    private boolean first;

    /** Indique si c'est la dernière page. */
    private boolean last;

    /**
     * Construit un PageResponse à partir d'un Page Spring Data
     * en appliquant une fonction de conversion à chaque élément.
     *
     * @param page     La page Spring Data source
     * @param mapper   La fonction de conversion Entity → DTO
     * @param <S>      Type source (entité)
     * @param <T>      Type cible (DTO)
     */
    public static <S, T> PageResponse<T> of(Page<S> page, Function<S, T> mapper) {
        List<T> content = page.getContent()
                .stream()
                .map(mapper)
                .collect(Collectors.toList());

        return new PageResponse<>(
                content,
                page.getTotalElements(),
                page.getTotalPages(),
                page.getNumber(),
                page.getSize(),
                page.isFirst(),
                page.isLast()
        );
    }
}
