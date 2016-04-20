package com.globigdata.neo4j.util;

import org.neo4j.kernel.impl.query.QuerySession;
import org.neo4j.kernel.impl.query.TransactionalContext;


/**
 * Created by WangTao on 2016/4/19.
 */
public class TcpServerQuerySession extends QuerySession{

    public TcpServerQuerySession( TransactionalContext transactionalContext )
    {
        super( transactionalContext );
    }

    @Override
    public String toString() {
        return "";
    }
}
