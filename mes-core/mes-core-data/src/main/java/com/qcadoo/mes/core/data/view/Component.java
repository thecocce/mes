package com.qcadoo.mes.core.data.view;

import java.util.Map;
import java.util.Set;

import com.qcadoo.mes.core.data.beans.Entity;
import com.qcadoo.mes.core.data.model.DataDefinition;

public interface Component<T> extends CastableComponent<T>, ListenableComponent, InitializableComponent {

    String getType();

    ViewValue<T> getValue(Entity entity, Map<String, Entity> selectedEntities, ViewValue<?> viewEntity,
            Set<String> pathsToUpdate);

    String getName();

    String getSourceFieldPath();

    String getPath();

    String getFieldPath();

    DataDefinition getDataDefinition();

}