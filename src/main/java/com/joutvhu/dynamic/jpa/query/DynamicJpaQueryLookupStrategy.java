package com.joutvhu.dynamic.jpa.query;

import com.joutvhu.dynamic.jpa.DynamicQuery;
import org.springframework.data.jpa.provider.QueryExtractor;
import org.springframework.data.jpa.repository.query.JpaQueryLookupStrategy;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.repository.core.NamedQueries;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.query.EvaluationContextProvider;
import org.springframework.data.repository.query.QueryLookupStrategy;
import org.springframework.data.repository.query.RepositoryQuery;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import javax.persistence.EntityManager;
import java.lang.reflect.Method;

/**
 * {@link QueryLookupStrategy} that tries to detect a dynamic query declared via {@link DynamicQuery} annotation.
 *
 * @author Giao Ho
 * @since 2.x.1
 */
public class DynamicJpaQueryLookupStrategy implements QueryLookupStrategy {
    private final EntityManager entityManager;
    private QueryExtractor extractor;
    private QueryLookupStrategy jpaQueryLookupStrategy;
    private EvaluationContextProvider evaluationContextProvider;

    public DynamicJpaQueryLookupStrategy(EntityManager entityManager, @Nullable Key key, QueryExtractor extractor,
                                         EvaluationContextProvider evaluationContextProvider) {
        this.jpaQueryLookupStrategy = JpaQueryLookupStrategy
                .create(entityManager, key, extractor, evaluationContextProvider);
        this.extractor = extractor;
        this.entityManager = entityManager;
        this.evaluationContextProvider = evaluationContextProvider;
    }

    @Override
    public RepositoryQuery resolveQuery(Method method, RepositoryMetadata metadata, ProjectionFactory factory, NamedQueries namedQueries) {
        if (isMethodDynamicJpaHandle(method)) {
            DynamicJpaQueryMethod queryMethod = new DynamicJpaQueryMethod(method, metadata, factory, extractor);
            return new DynamicJpaRepositoryQuery(queryMethod, entityManager, evaluationContextProvider);
        } else return jpaQueryLookupStrategy.resolveQuery(method, metadata, factory, namedQueries);
    }

    private boolean isMethodDynamicJpaHandle(Method method) {
        DynamicQuery annotation = method.getAnnotation(DynamicQuery.class);
        return annotation != null;
    }

    public static QueryLookupStrategy create(EntityManager entityManager, @Nullable Key key, QueryExtractor extractor,
                                             EvaluationContextProvider evaluationContextProvider) {
        Assert.notNull(entityManager, "EntityManager must not be null!");
        Assert.notNull(extractor, "QueryExtractor must not be null!");
        Assert.notNull(evaluationContextProvider, "EvaluationContextProvider must not be null!");

        return new DynamicJpaQueryLookupStrategy(entityManager, key, extractor, evaluationContextProvider);
    }
}
