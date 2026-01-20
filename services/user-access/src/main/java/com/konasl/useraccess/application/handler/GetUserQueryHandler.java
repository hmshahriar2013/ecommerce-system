package com.konasl.useraccess.application.handler;

import com.konasl.common.cqrs.QueryHandler;
import com.konasl.useraccess.application.port.UserReadRepository;
import com.konasl.useraccess.application.query.GetUserQuery;
import com.konasl.useraccess.application.query.UserDto;
import org.springframework.stereotype.Component;

/**
 * Handler for GetUserQuery.
 */
@Component
public class GetUserQueryHandler implements QueryHandler<GetUserQuery, UserDto> {

    private final UserReadRepository readRepository;

    public GetUserQueryHandler(UserReadRepository readRepository) {
        this.readRepository = readRepository;
    }

    @Override
    public UserDto handle(GetUserQuery query) {
        return readRepository.findById(query.userId())
                .orElseThrow(() -> new RuntimeException("User not found: " + query.userId()));
    }

    @Override
    public Class<GetUserQuery> getQueryType() {
        return GetUserQuery.class;
    }
}
