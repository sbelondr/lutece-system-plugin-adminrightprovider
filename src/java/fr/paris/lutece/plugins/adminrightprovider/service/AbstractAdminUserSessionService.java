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
package fr.paris.lutece.plugins.adminrightprovider.service;

import fr.paris.lutece.portal.business.rbac.RBACRole;
import fr.paris.lutece.portal.business.rbac.RBACRoleHome;
import fr.paris.lutece.portal.business.right.LevelHome;
import fr.paris.lutece.portal.business.right.Right;
import fr.paris.lutece.portal.business.right.RightHome;
import fr.paris.lutece.portal.business.user.AdminUser;
import fr.paris.lutece.portal.business.user.AdminUserHome;
import fr.paris.lutece.portal.business.user.PasswordUpdateMode;
import fr.paris.lutece.portal.business.user.authentication.LuteceDefaultAdminUser;
import fr.paris.lutece.portal.business.workgroup.AdminWorkgroupHome;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractAdminUserSessionService implements IAdminUserSessionService
{

    private static final String PROPERTY_DEFAULT_RIGHTS = "adminrightprovider.default.rights.list";
    private static final String PROPERTY_DEFAULT_ROLES = "adminrightprovider.default.roles.list";
    private static final String PROPERTY_DEFAULT_WORKGROUPS = "adminrightprovider.default.workgroups.list";
    private static final String PROPERTY_DEFAULT_LEVEL = "adminrightprovider.default.level";
    private static final String PROPERTY_ACTIVE_FOR_ADMIN = "adminrightprovider.activeForAdmin";

    private static final String [ ] DEFAULT_RIGHTS_LIST = AppPropertiesService.getProperty( PROPERTY_DEFAULT_RIGHTS, "" ).split( "," );
    private static final String [ ] DEFAULT_ROLES_LIST = AppPropertiesService.getProperty( PROPERTY_DEFAULT_ROLES, "" ).split( "," );
    private static final String [ ] DEFAULT_WORKGROUPS_LIST = AppPropertiesService.getProperty( PROPERTY_DEFAULT_WORKGROUPS, "" ).split( "," );
    private static final int DEFAULT_LEVEL = AppPropertiesService.getPropertyInt( PROPERTY_DEFAULT_LEVEL, -1 );
    private static final boolean ACTIVE_FOR_ADMIN = AppPropertiesService.getPropertyBoolean( PROPERTY_ACTIVE_FOR_ADMIN, false );

    private static List<Right> _lstDefaultDbRights;
    private static List<RBACRole> _lstDefaultDbRoles;
    private static List<String> _lstDefaultDbWorkgroups;
    private static Integer _defaultExistingLevel;

    protected static List<Right> getDefaultRights( )
    {
        _lstDefaultDbRights = new ArrayList<>( );
        for ( String strRight : DEFAULT_RIGHTS_LIST )
        {
            Right right = RightHome.findByPrimaryKey( strRight );
            if ( right != null )
            {
                _lstDefaultDbRights.add( right );
            }
        }
        return _lstDefaultDbRights;
    }

    protected static List<RBACRole> getDefaultRoles( )
    {
        _lstDefaultDbRoles = new ArrayList<>( );
        for ( String strRole : DEFAULT_ROLES_LIST )
        {
            RBACRole rbacRole = RBACRoleHome.findByPrimaryKey( strRole );
            if ( rbacRole != null )
            {
                _lstDefaultDbRoles.add( rbacRole );
            }
        }
        return _lstDefaultDbRoles;
    }

    protected static List<String> getDefaultWorkgroups( )
    {
        _lstDefaultDbWorkgroups = new ArrayList<>( );
        for ( String strWorkgroup : DEFAULT_WORKGROUPS_LIST )
        {
            if ( AdminWorkgroupHome.checkExistWorkgroup( strWorkgroup ) )
            {
                _lstDefaultDbWorkgroups.add( strWorkgroup );
            }
        }
        return _lstDefaultDbWorkgroups;
    }

    protected static int getDefaultLevel( )
    {
        if ( LevelHome.findByPrimaryKey( DEFAULT_LEVEL ) != null )
        {
            _defaultExistingLevel = DEFAULT_LEVEL;
        }
        else
        {
            _defaultExistingLevel = -1;
        }
        return _defaultExistingLevel;
    }

    protected boolean isNewUser( AdminUser sessionUser )
    {
        return sessionUser.getStatus( ) == AdminUser.ACTIVE_CODE && sessionUser.getUserRoles( ).isEmpty( ) && sessionUser.getUserWorkgroups( ).isEmpty( )
                && sessionUser.getRights( ).isEmpty( );
    }

    @Override
    public AdminUser updateUser( int nUserId )
    {
        AdminUser createdUser = AdminUserHome.findByPrimaryKey( nUserId );

        if ( getDefaultLevel( ) >= 0 && getDefaultLevel( ) != createdUser.getUserLevel( ) )
        {
            createdUser.setUserLevel( getDefaultLevel( ) );
            // unable to update level without using LuteceDefaultAdminUser
            LuteceDefaultAdminUser luteceDefaultAdminUser = new LuteceDefaultAdminUser( );
            BeanUtils.copyProperties( createdUser, luteceDefaultAdminUser );
            AdminUserHome.update( luteceDefaultAdminUser, PasswordUpdateMode.IGNORE );
        }

        for ( RBACRole role : getDefaultRoles( ) )
        {
            AdminUserHome.createRoleForUser( createdUser.getUserId( ), role.getKey( ) );
        }

        for ( Right right : getDefaultRights( ) )
        {
            AdminUserHome.createRightForUser( createdUser.getUserId( ), right.getId( ) );
        }

        for ( String strWorkgroup : getDefaultWorkgroups( ) )
        {
            AdminWorkgroupHome.addUserForWorkgroup( createdUser, strWorkgroup );
        }

        // set the rights for this user
        createdUser.setRights( AdminUserHome.getRightsListForUser( createdUser.getUserId( ) ) );

        // set the rights for this user
        createdUser.setRoles( AdminUserHome.getRolesListForUser( createdUser.getUserId( ) ) );

        // set the workgroups for this user
        createdUser.setUserWorkgroups( getDefaultWorkgroups( ) );

        return createdUser;
    }

    @Override
    public boolean shouldUpdateUser( AdminUser sessionUser )
    {
        if ( getDefaultRights( ).isEmpty( ) && getDefaultRoles( ).isEmpty( ) && getDefaultWorkgroups( ).isEmpty( ) && getDefaultLevel( ) == -1 )
        {
            return false;
        }

        if ( !ACTIVE_FOR_ADMIN && sessionUser.isAdmin( ) )
        {
            return false;
        }

        if ( sessionUser.getStatus( ) != AdminUser.ACTIVE_CODE )
        {
            return false;
        }

        return isNewUser( sessionUser );
    }
}
