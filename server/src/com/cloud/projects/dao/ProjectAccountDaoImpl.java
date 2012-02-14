/**
 * Copyright (C) 2011 Citrix Systems, Inc.  All rights reserved
 * 
 * This software is licensed under the GNU General Public License v3 or later.
 * 
 * It is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package com.cloud.projects.dao;

import java.util.List;

import javax.ejb.Local;

import org.apache.log4j.Logger;

import com.cloud.projects.ProjectAccount;
import com.cloud.projects.ProjectAccountVO;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.GenericSearchBuilder;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import com.cloud.utils.db.SearchCriteria.Func;
import com.cloud.utils.db.SearchCriteria.Op;

@Local(value = { ProjectAccountDao.class })
public class ProjectAccountDaoImpl extends GenericDaoBase<ProjectAccountVO, Long> implements ProjectAccountDao {
    protected final SearchBuilder<ProjectAccountVO> AllFieldsSearch;
    final GenericSearchBuilder<ProjectAccountVO, Long> AdminSearch;
    final GenericSearchBuilder<ProjectAccountVO, Long> ProjectAccountSearch;
    final GenericSearchBuilder<ProjectAccountVO, Long> CountByRoleSearch;
    public static final Logger s_logger = Logger.getLogger(ProjectAccountDaoImpl.class.getName());

    protected ProjectAccountDaoImpl() {
        AllFieldsSearch = createSearchBuilder();
        AllFieldsSearch.and("role", AllFieldsSearch.entity().getAccountRole(), SearchCriteria.Op.EQ);
        AllFieldsSearch.and("projectId", AllFieldsSearch.entity().getProjectId(), SearchCriteria.Op.EQ);
        AllFieldsSearch.and("accountId", AllFieldsSearch.entity().getAccountId(), SearchCriteria.Op.EQ);
        AllFieldsSearch.and("projectAccountId", AllFieldsSearch.entity().getProjectAccountId(), SearchCriteria.Op.EQ);
        AllFieldsSearch.done();

        AdminSearch = createSearchBuilder(Long.class);
        AdminSearch.selectField(AdminSearch.entity().getProjectId());
        AdminSearch.and("role", AdminSearch.entity().getAccountRole(), Op.EQ);
        AdminSearch.and("accountId", AdminSearch.entity().getAccountId(), Op.EQ);
        AdminSearch.done();

        ProjectAccountSearch = createSearchBuilder(Long.class);
        ProjectAccountSearch.selectField(ProjectAccountSearch.entity().getProjectAccountId());
        ProjectAccountSearch.and("accountId", ProjectAccountSearch.entity().getAccountId(), Op.EQ);
        ProjectAccountSearch.done();

        CountByRoleSearch = createSearchBuilder(Long.class);
        CountByRoleSearch.select(null, Func.COUNT, CountByRoleSearch.entity().getId());
        CountByRoleSearch.and("accountId", CountByRoleSearch.entity().getAccountId(), Op.EQ);
        CountByRoleSearch.and("role", CountByRoleSearch.entity().getAccountRole(), Op.EQ);
        CountByRoleSearch.done();
    }

    @Override
    public ProjectAccountVO getProjectOwner(long projectId) {
        SearchCriteria<ProjectAccountVO> sc = AllFieldsSearch.create();
        sc.setParameters("role", ProjectAccount.Role.Admin);
        sc.setParameters("projectId", projectId);

        return findOneBy(sc);
    }

    @Override
    public List<ProjectAccountVO> listByProjectId(long projectId) {
        SearchCriteria<ProjectAccountVO> sc = AllFieldsSearch.create();
        sc.setParameters("projectId", projectId);

        return listBy(sc);
    }

    @Override
    public ProjectAccountVO findByProjectIdAccountId(long projectId, long accountId) {
        SearchCriteria<ProjectAccountVO> sc = AllFieldsSearch.create();
        sc.setParameters("projectId", projectId);
        sc.setParameters("accountId", accountId);

        return findOneBy(sc);
    }

    @Override
    public boolean canAccessProjectAccount(long accountId, long projectAccountId) {
        SearchCriteria<ProjectAccountVO> sc = AllFieldsSearch.create();
        sc.setParameters("accountId", accountId);
        sc.setParameters("projectAccountId", projectAccountId);

        if (findOneBy(sc) != null) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean canModifyProjectAccount(long accountId, long projectAccountId) {
        SearchCriteria<ProjectAccountVO> sc = AllFieldsSearch.create();
        sc.setParameters("accountId", accountId);
        sc.setParameters("projectAccountId", projectAccountId);
        sc.setParameters("role", ProjectAccount.Role.Admin);

        if (findOneBy(sc) != null) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public List<Long> listPermittedAccountIds(long accountId) {
        SearchCriteria<Long> sc = ProjectAccountSearch.create();
        sc.setParameters("accountId", accountId);
        return customSearch(sc, null);
    }

    @Override
    public List<Long> listAdministratedProjectIds(long adminAccountId) {
        SearchCriteria<Long> sc = AdminSearch.create();
        sc.setParameters("role", ProjectAccount.Role.Admin);
        sc.setParameters("accountId", adminAccountId);
        return customSearch(sc, null);
    }

    @Override
    public Long countByAccountIdAndRole(long accountId, ProjectAccount.Role role) {
        SearchCriteria<Long> sc = CountByRoleSearch.create();
        sc.setParameters("accountId", accountId);
        sc.setParameters("role", role);
        return customSearch(sc, null).get(0);
    }
    
    @Override
    public void removeAccountFromProjects(long accountId) {
        SearchCriteria<ProjectAccountVO> sc = AllFieldsSearch.create();
        sc.setParameters("accountId", accountId);

        int rowsRemoved = remove(sc);
        if (rowsRemoved > 0) {
            s_logger.debug("Removed account id=" + accountId + " from " + rowsRemoved + " projects");
        }
    }

}
