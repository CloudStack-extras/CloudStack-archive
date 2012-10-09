// Copyright 2012 Citrix Systems, Inc. Licensed under the
// Apache License, Version 2.0 (the "License"); you may not use this
// file except in compliance with the License.  Citrix Systems, Inc.
// reserves all rights not expressly granted by the License.
// You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
package com.cloud.network.as;

import java.util.List;

import com.cloud.acl.ControlledEntity;
import com.cloud.utils.Pair;

/**
 * AutoScaleVmProfile
 */
public interface AutoScaleVmProfile extends ControlledEntity {

    public long getId();

    public Long getZoneId();

    public Long getServiceOfferingId();

    public Long getTemplateId();

    public String getOtherDeployParams();

    List<Pair<String, String>> getCounterParams();

    public Integer getDestroyVmGraceperiod();

    public long getAutoScaleUserId();

}
