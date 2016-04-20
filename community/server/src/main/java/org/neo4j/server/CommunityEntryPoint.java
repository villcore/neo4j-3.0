/*
 * Copyright (c) 2002-2016 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.neo4j.server;

public class CommunityEntryPoint
{
    public static final boolean isRunInLinux = false;
    public static final boolean isRunTcpServer = true;

    private static Bootstrapper bootstrapper;

    public static void main( String[] args )
    {
        //在linux下 ./neo4j start命令传入的参数为 --config-dir=conf
        //文件指向conf/neo4j.conf

        if(CommunityEntryPoint.isRunInLinux) {
            int status = ServerBootstrapper.start( new CommunityBootstrapper(), args );
            if ( status != 0 )
            {
                System.exit( status );
            }
        }
        else {
            String mockArgs = "--config-dir=conf";
            int status = ServerBootstrapper.start( new CommunityBootstrapper(), mockArgs);
            if ( status != 0 )
            {
                System.exit( status );
            }
        }
    }

    public static void start( String[] args )
    {
        bootstrapper = new BlockingBootstrapper( new CommunityBootstrapper() );
        System.exit( ServerBootstrapper.start( bootstrapper, args ) );
    }

    public static void stop( @SuppressWarnings("UnusedParameters") String[] args )
    {
        if ( bootstrapper != null )
        {
            bootstrapper.stop();
        }
    }
}
