/*
 * Copyright (c) 2002-2023, City of Paris
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  1. Redistributions of source code must retain the above copyright notice
 *     and the following disclaimer.
 *
 *  2. Redistributions in binary form must reproduce the above copyright notice
 *     and the following disclaimer in the documentation and/or other materials
 *     provided with the distribution.
 *
 *  3. Neither the name of 'Mairie de Paris' nor 'Lutece' nor the names of its
 *     contributors may be used to endorse or promote products derived from
 *     this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * License 1.0
 */
package fr.paris.lutece.plugins.adminrightprovider.service.listener;

import fr.paris.lutece.plugins.adminrightprovider.service.IAdminUserSessionService;
import fr.paris.lutece.portal.business.user.AdminUser;
import fr.paris.lutece.portal.service.spring.SpringContextService;

import javax.servlet.http.HttpSessionAttributeListener;
import javax.servlet.http.HttpSessionBindingEvent;

/**
 * AdminUserSessionListener
 */
public final class AdminUserSessionListener implements HttpSessionAttributeListener
{
    private static final String ATTRIBUTE_ADMIN_USER = "lutece_admin_user";
    private static IAdminUserSessionService _adminUserSessionService = null;

    private static IAdminUserSessionService getAdminUserSessionService( )
    {
        if ( _adminUserSessionService == null )
        {
            _adminUserSessionService = SpringContextService.getBean( IAdminUserSessionService.BEAN_NAME );
        }
        return _adminUserSessionService;
    }

    @Override
    public void attributeAdded( HttpSessionBindingEvent event )
    {
        if ( ATTRIBUTE_ADMIN_USER.equals( event.getName( ) ) )
        {
            AdminUser user = (AdminUser) event.getValue( );
            if ( getAdminUserSessionService( ).shouldUpdateUser( user ) )
            {
                event.getSession( ).setAttribute( ATTRIBUTE_ADMIN_USER, getAdminUserSessionService( ).updateUser( user.getUserId( ) ) );
            }
        }
    }

    @Override
    public void attributeRemoved( HttpSessionBindingEvent event )
    {
        // Nothing to do
    }

    @Override
    public void attributeReplaced( HttpSessionBindingEvent event )
    {
        // Nothing to do
    }
}
