/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.1
 *
 * This file is part of Qcadoo.
 *
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */

package com.qcadoo.mes.view.xml;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.hasItems;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.test.util.ReflectionTestUtils.getField;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.matchers.JUnitMatchers;
import org.springframework.context.ApplicationContext;
import org.springframework.util.StringUtils;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.qcadoo.mes.api.DataDefinitionService;
import com.qcadoo.mes.api.PluginManagementService;
import com.qcadoo.mes.api.ViewDefinitionService;
import com.qcadoo.mes.beans.plugins.PluginsPlugin;
import com.qcadoo.mes.beans.sample.CustomEntityService;
import com.qcadoo.mes.internal.ViewDefinitionServiceImpl;
import com.qcadoo.mes.model.DataDefinition;
import com.qcadoo.mes.model.FieldDefinition;
import com.qcadoo.mes.model.HookDefinition;
import com.qcadoo.mes.model.hooks.internal.HookFactory;
import com.qcadoo.mes.model.types.BelongsToType;
import com.qcadoo.mes.model.types.HasManyType;
import com.qcadoo.mes.model.types.internal.StringType;
import com.qcadoo.mes.view.AbstractComponent;
import com.qcadoo.mes.view.Component;
import com.qcadoo.mes.view.ContainerComponent;
import com.qcadoo.mes.view.RootComponent;
import com.qcadoo.mes.view.ViewDefinition;
import com.qcadoo.mes.view.components.CheckBoxComponent;
import com.qcadoo.mes.view.components.GridComponent;
import com.qcadoo.mes.view.components.LinkButtonComponent;
import com.qcadoo.mes.view.components.LookupComponent;
import com.qcadoo.mes.view.components.TextAreaComponent;
import com.qcadoo.mes.view.components.grid.ColumnAggregationMode;
import com.qcadoo.mes.view.components.grid.ColumnDefinition;
import com.qcadoo.mes.view.containers.FormComponent;
import com.qcadoo.mes.view.containers.WindowComponent;
import com.qcadoo.mes.view.internal.ViewDefinitionParser;
import com.qcadoo.mes.view.menu.ribbon.Ribbon;
import com.qcadoo.mes.view.menu.ribbon.RibbonActionItem;
import com.qcadoo.mes.view.menu.ribbon.RibbonComboItem;

public class ViewDefinitionParserTest {

    private ViewDefinitionParser viewDefinitionParser;

    private DataDefinitionService dataDefinitionService;

    private ViewDefinitionService viewDefinitionService;

    private HookFactory hookFactory;

    private ApplicationContext applicationContext;

    private PluginManagementService pluginManagementService;

    private InputStream xml;

    private DataDefinition dataDefinitionA;

    private DataDefinition dataDefinitionB;

    @Before
    public void init() throws Exception {
        applicationContext = mock(ApplicationContext.class);
        dataDefinitionService = mock(DataDefinitionService.class);

        pluginManagementService = mock(PluginManagementService.class);

        viewDefinitionService = new ViewDefinitionServiceImpl();

        hookFactory = new HookFactory();
        setField(hookFactory, "applicationContext", applicationContext);

        viewDefinitionParser = new ViewDefinitionParser();
        setField(viewDefinitionParser, "dataDefinitionService", dataDefinitionService);
        setField(viewDefinitionParser, "viewDefinitionService", viewDefinitionService);
        setField(viewDefinitionParser, "hookFactory", hookFactory);

        xml = new FileInputStream(new File("src/test/resources/view.xml"));

        PluginsPlugin plugin = new PluginsPlugin();
        plugin.setIdentifier("sample");

        given(pluginManagementService.getByIdentifierAndStatus("sample", "active")).willReturn(plugin);
        given(applicationContext.getBean(CustomEntityService.class)).willReturn(new CustomEntityService());

        dataDefinitionA = mock(DataDefinition.class);
        dataDefinitionB = mock(DataDefinition.class);
        FieldDefinition nameA = mock(FieldDefinition.class);
        FieldDefinition nameB = mock(FieldDefinition.class);
        FieldDefinition hasManyB = mock(FieldDefinition.class);
        FieldDefinition belongToA = mock(FieldDefinition.class);
        HasManyType hasManyBType = mock(HasManyType.class);
        BelongsToType belongToAType = mock(BelongsToType.class);

        given(nameA.getType()).willReturn(new StringType());
        given(nameB.getType()).willReturn(new StringType());
        given(hasManyB.getType()).willReturn(hasManyBType);
        given(belongToA.getType()).willReturn(belongToAType);
        given(hasManyBType.getDataDefinition()).willReturn(dataDefinitionB);
        given(belongToAType.getDataDefinition()).willReturn(dataDefinitionA);
        given(dataDefinitionA.getField("beansB")).willReturn(hasManyB);
        given(dataDefinitionA.getField("name")).willReturn(nameA);
        given(dataDefinitionB.getField("beanA")).willReturn(belongToA);
        given(dataDefinitionB.getField("name")).willReturn(nameB);
        given(dataDefinitionA.getName()).willReturn("beanA");
        given(dataDefinitionB.getName()).willReturn("beanB");
        given(dataDefinitionA.getFields()).willReturn(ImmutableMap.of("name", nameA, "beansB", hasManyB));
        given(dataDefinitionB.getFields()).willReturn(ImmutableMap.of("name", nameB, "beanA", belongToA));
        given(dataDefinitionService.get("sample", "beanA")).willReturn(dataDefinitionA);
        given(dataDefinitionService.get("sample", "beanB")).willReturn(dataDefinitionB);
    }

    @Test
    public void shouldParseXml() {
        // given
        ViewDefinition viewDefinition = parseAndGetViewDefinition();
        assertEquals(2, viewDefinitionService.list().size());

        // then
        assertNotNull(viewDefinition);
    }

    @Test
    public void shouldSetViewDefinitionAttributes() {
        // given
        ViewDefinition viewDefinition = parseAndGetViewDefinition();

        // then
        assertEquals("simpleView", viewDefinition.getName());
        assertEquals("sample", viewDefinition.getPluginIdentifier());
        assertThat(getField(viewDefinition, "root"), instanceOf(WindowComponent.class));
    }

    @Test
    public void shouldHasRibbon() throws Exception {
        // given
        ViewDefinition viewDefinition = parseAndGetViewDefinition();
        Ribbon ribbon = (Ribbon) getField(getField(viewDefinition, "root"), "ribbon");

        // then
        assertNotNull(ribbon);
        assertEquals(2, ribbon.getGroups().size());
        assertEquals("first", ribbon.getGroups().get(0).getName());
        assertEquals(2, ribbon.getGroups().get(0).getItems().size());
        assertEquals("second", ribbon.getGroups().get(1).getName());
        assertEquals(2, ribbon.getGroups().get(1).getItems().size());

        assertEquals("test", ribbon.getGroups().get(0).getItems().get(0).getName());
        assertNull(ribbon.getGroups().get(0).getItems().get(0).getIcon());
        assertEquals(RibbonActionItem.Type.BIG_BUTTON, ribbon.getGroups().get(0).getItems().get(0).getType());
        assertEquals("#{mainWindow.beanBForm}.save,#{mainWindow}.back", (ribbon.getGroups().get(0).getItems().get(0)).getAction());
        assertEquals("test2", ribbon.getGroups().get(0).getItems().get(1).getName());
        assertEquals("icon2", ribbon.getGroups().get(0).getItems().get(1).getIcon());
        assertEquals(RibbonActionItem.Type.SMALL_BUTTON, ribbon.getGroups().get(0).getItems().get(1).getType());
        assertEquals("xxx", (ribbon.getGroups().get(0).getItems().get(1)).getAction());

        assertEquals("test2", ribbon.getGroups().get(1).getItems().get(0).getName());
        assertNull(ribbon.getGroups().get(1).getItems().get(0).getIcon());
        assertEquals(RibbonActionItem.Type.BIG_BUTTON, ribbon.getGroups().get(1).getItems().get(0).getType());
        assertNull((ribbon.getGroups().get(1).getItems().get(0)).getAction());

        assertEquals("combo1", ribbon.getGroups().get(1).getItems().get(1).getName());
        assertNull(ribbon.getGroups().get(1).getItems().get(1).getIcon());
        assertEquals("yyy3", ribbon.getGroups().get(1).getItems().get(1).getAction());
        assertEquals(RibbonActionItem.Type.BIG_BUTTON, ribbon.getGroups().get(1).getItems().get(1).getType());

        List<RibbonActionItem> items = ((RibbonComboItem) ribbon.getGroups().get(1).getItems().get(1)).getItems();

        assertEquals("test1", items.get(0).getName());
        assertEquals(RibbonActionItem.Type.BIG_BUTTON, items.get(0).getType());
        assertEquals("yyy1", items.get(0).getAction());
        assertNull(items.get(0).getIcon());

        assertEquals("test2", items.get(1).getName());
        assertEquals(RibbonActionItem.Type.BIG_BUTTON, items.get(1).getType());
        assertEquals("yyy2", items.get(1).getAction());
        assertEquals("icon2", items.get(1).getIcon());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldSetFields() {
        // given
        ViewDefinition viewDefinition = parseAndGetViewDefinition();
        RootComponent root = (RootComponent) getField(viewDefinition, "root");

        // then
        checkComponent(root, WindowComponent.class, "mainWindow", "window", "beanB", null, null, null,
                Sets.<String> newHashSet(), 3, ImmutableMap.<String, Object> of("backButton", false, "header", true));

        checkComponent(root.lookupComponent("mainWindow.beanBForm"), FormComponent.class, "mainWindow.beanBForm", "form",
                "beanB", null, null, null, Sets.<String> newHashSet(), 7, ImmutableMap.<String, Object> of("header", false));

        checkComponent(root.lookupComponent("mainWindow.beanBForm.name"), TextAreaComponent.class, "mainWindow.beanBForm.name",
                "textArea", "beanB", "name", null, null, Sets.<String> newHashSet(), 0, Maps.<String, Object> newHashMap());

        Component<?> selectBeanA = root.lookupComponent("mainWindow.beanBForm.selectBeanA");
        checkComponent(selectBeanA, LookupComponent.class, "mainWindow.beanBForm.selectBeanA", "lookupComponent", "beanA",
                "beanA", null, null, Sets.newHashSet("mainWindow.beanBForm.beansBGrig", "mainWindow.beansBGrig"), 0,
                Maps.<String, Object> newHashMap());

        assertTrue(selectBeanA.isDefaultEnabled());
        assertTrue(selectBeanA.isDefaultVisible());

        Component<?> active = root.lookupComponent("mainWindow.beanBForm.active");
        checkComponent(active, CheckBoxComponent.class, "mainWindow.beanBForm.active", "checkBox", "beanB", "name", null, null,
                Sets.<String> newHashSet(), 0, Maps.<String, Object> newHashMap());

        assertFalse(active.isDefaultEnabled());
        assertFalse(active.isDefaultVisible());

        checkComponent(root.lookupComponent("mainWindow.beanBForm.beanM"), TextAreaComponent.class, "mainWindow.beanBForm.beanM",
                "textArea", "beanB", "name", null, null, Sets.<String> newHashSet(), 0, Maps.<String, Object> newHashMap());

        checkComponent(root.lookupComponent("mainWindow.beanBForm.beanB"), TextAreaComponent.class, "mainWindow.beanBForm.beanB",
                "textArea", "beanA", "beanA.name", null, null, Sets.<String> newHashSet(), 0, Maps.<String, Object> newHashMap());

        checkComponent(root.lookupComponent("mainWindow.beanBForm.beanAForm"), FormComponent.class,
                "mainWindow.beanBForm.beanAForm", "form", "beanA", "beanA", null, null, Sets.<String> newHashSet(), 1,
                Maps.<String, Object> newHashMap());

        checkComponent(root.lookupComponent("mainWindow.beanBForm.beanAForm.name"), TextAreaComponent.class,
                "mainWindow.beanBForm.beanAForm.name", "textArea", "beanA", "name", null, null, Sets.<String> newHashSet(), 0,
                Maps.<String, Object> newHashMap());

        GridComponent grid = (GridComponent) root.lookupComponent("mainWindow.beanBForm.beansBGrig");
        checkComponent(grid, GridComponent.class, "mainWindow.beanBForm.beansBGrig", "grid", "beanB", null,
                "mainWindow.beanBForm.selectBeanA", "beansB", Sets.<String> newHashSet(), 0, Maps.<String, Object> newHashMap());
        List<ColumnDefinition> columns = new ArrayList<ColumnDefinition>(grid.getColumns());

        // assertThat((List<ColumnDefinition>) grid.getOptions().get("columns"), hasItems("name"));
        assertThat((List<String>) grid.getOptions().get("fields"), hasItems("name", "beanA"));
        assertEquals("products/form", grid.getOptions().get("correspondingViewName"));
        assertTrue((Boolean) grid.getOptions().get("header"));
        assertFalse((Boolean) grid.getOptions().get("sortable"));
        assertFalse((Boolean) grid.getOptions().get("filter"));
        assertFalse((Boolean) grid.getOptions().get("multiselect"));
        assertFalse((Boolean) grid.getOptions().get("paginable"));
        assertFalse(grid.getOptions().containsKey("height"));
        assertFalse((Boolean) grid.getOptions().get("canDelete"));
        assertFalse((Boolean) grid.getOptions().get("canNew"));
        assertTrue(((Set<ColumnDefinition>) getField(grid, "orderableColumns")).isEmpty());
        assertTrue(((Set<FieldDefinition>) getField(grid, "searchableFields")).isEmpty());
        assertEquals("name", columns.get(0).getName());
        assertEquals(ColumnAggregationMode.NONE, columns.get(0).getAggregationMode());
        assertNull(columns.get(0).getExpression());
        assertNull(columns.get(0).getWidth());
        assertEquals(1, columns.get(0).getFields().size());
        assertThat(columns.get(0).getFields(), hasItems(dataDefinitionB.getField("name")));

        GridComponent grid2 = (GridComponent) root.lookupComponent("mainWindow.beansBGrig");
        checkComponent(grid2, GridComponent.class, "mainWindow.beansBGrig", "grid", "beanB", null,
                "mainWindow.beanBForm.selectBeanA", "beansB", Sets.<String> newHashSet(), 0, Maps.<String, Object> newHashMap());
        List<ColumnDefinition> columns2 = new ArrayList<ColumnDefinition>(grid2.getColumns());

        // assertThat((List<String>) grid2.getOptions().get("columns"), hasItems("name", "multicolumn"));
        assertThat((List<String>) grid2.getOptions().get("fields"), hasItems("name", "beanA"));
        assertEquals("products/form", grid2.getOptions().get("correspondingViewName"));
        assertTrue((Boolean) grid2.getOptions().get("header"));
        assertTrue((Boolean) grid2.getOptions().get("sortable"));
        assertTrue((Boolean) grid2.getOptions().get("filter"));
        assertTrue((Boolean) grid2.getOptions().get("multiselect"));
        assertTrue((Boolean) grid2.getOptions().get("paginable"));
        assertFalse((Boolean) grid2.getOptions().get("canDelete"));
        assertFalse((Boolean) grid2.getOptions().get("canNew"));
        assertEquals(Integer.valueOf(450), grid2.getOptions().get("height"));
        assertEquals(1, ((Set<String>) getField(grid2, "orderableColumns")).size());
        assertThat(((Set<String>) getField(grid2, "orderableColumns")), hasItems(columns2.get(0).getName()));
        assertEquals(2, ((Set<FieldDefinition>) getField(grid2, "searchableFields")).size());
        assertThat(((Set<FieldDefinition>) getField(grid2, "searchableFields")),
                hasItems(dataDefinitionB.getField("name"), dataDefinitionB.getField("beanA")));
        assertEquals("multicolumn", columns2.get(1).getName());
        assertEquals(ColumnAggregationMode.SUM, columns2.get(1).getAggregationMode());
        assertEquals("2 + 2", columns2.get(1).getExpression());
        assertEquals(Integer.valueOf(20), columns2.get(1).getWidth());
        assertEquals(2, columns2.get(1).getFields().size());
        assertThat(columns2.get(1).getFields(), hasItems(dataDefinitionB.getField("name"), dataDefinitionB.getField("beanA")));

        checkComponent(root.lookupComponent("mainWindow.link"), LinkButtonComponent.class, "mainWindow.link", "linkButton",
                "beanB", null, null, null, Sets.<String> newHashSet(), 0,
                ImmutableMap.<String, Object> of("url", "download.html"));
    }

    private void checkComponent(final Component<?> component2, final Class<?> clazz, final String path, final String type,
            final String model, final String fieldPath, final String sourceComponentPath, final String sourceFieldPath,
            final Set<String> listeners, final int children, final Map<String, Object> options) {
        AbstractComponent<?> component = (AbstractComponent<?>) component2;
        String name = path.split("\\.")[path.split("\\.").length - 1];
        assertNotNull(component);
        assertThat(component, instanceOf(clazz));
        assertEquals(name, component.getName());
        assertEquals(fieldPath, component.getFieldPath());
        assertEquals(listeners.size(), component.getListeners().size());
        if (listeners.size() > 0) {
            assertThat(component.getListeners(), JUnitMatchers.hasItems(listeners.toArray(new String[listeners.size()])));
        }
        assertEquals(path, component.getPath());
        assertEquals(sourceFieldPath, component.getSourceFieldPath());
        if (StringUtils.hasText(sourceComponentPath)) {
            assertEquals(sourceComponentPath, ((Component<?>) getField(component, "sourceComponent")).getPath());
        } else {
            assertNull(getField(component, "sourceComponent"));
        }

        assertEquals(model, component.getDataDefinition().getName());
        assertEquals(type, component.getType());
        assertEquals("simpleView", component.getViewDefinition().getName());

        if (component instanceof ContainerComponent) {
            assertEquals(children, ((ContainerComponent<?>) component).getComponents().size());
        }

        for (Map.Entry<String, Object> option : options.entrySet()) {
            assertEquals(option.getValue(), component.getOptions().get(option.getKey()));
        }
    }

    @Test
    public void shouldSetHooks() {
        // given
        ViewDefinition viewDefinition = parseAndGetViewDefinition();

        // then
        testHookDefinition(viewDefinition, "viewHook", CustomEntityService.class, "onView");
    }

    private void testHookDefinition(final Object object, final String hookFieldName, final Class<?> hookBeanClass,
            final String hookMethodName) {
        HookDefinition hook = (HookDefinition) getField(object, hookFieldName);

        assertNotNull(hook);
        assertThat(getField(hook, "bean"), instanceOf(hookBeanClass));
        assertEquals(hookMethodName, getField(hook, "methodName"));
    }

    private ViewDefinition parseAndGetViewDefinition() {
        viewDefinitionParser.parse(xml);
        return viewDefinitionService.get("sample", "simpleView");
    }

}