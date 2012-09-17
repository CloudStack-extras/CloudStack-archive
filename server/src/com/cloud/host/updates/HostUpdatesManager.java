package com.cloud.host.updates;

import java.util.List;

import org.w3c.dom.Element;

import com.cloud.utils.component.Manager;

public interface HostUpdatesManager extends Manager {

    void fillUpdates(Element patchDetailsNode, List<String> releasedPatches, long hostId);

    void updateAppliedField(List<String> appliedPatchList, long hostId);

}
