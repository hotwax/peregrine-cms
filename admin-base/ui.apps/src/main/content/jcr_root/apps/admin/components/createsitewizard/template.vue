<!--
  #%L
  admin base - UI Apps
  %%
  Copyright (C) 2017 headwire inc.
  %%
  Licensed to the Apache Software Foundation (ASF) under one
  or more contributor license agreements.  See the NOTICE file
  distributed with this work for additional information
  regarding copyright ownership.  The ASF licenses this file
  to you under the Apache License, Version 2.0 (the
  "License"); you may not use this file except in compliance
  with the License.  You may obtain a copy of the License at
  
  http://www.apache.org/licenses/LICENSE-2.0
  
  Unless required by applicable law or agreed to in writing,
  software distributed under the License is distributed on an
  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  KIND, either express or implied.  See the License for the
  specific language governing permissions and limitations
  under the License.
  #L%
  -->
<template>
<div class="container">
    <form-wizard
      v-bind:title="'create a site'"
      v-bind:subtitle="''" @on-complete="onComplete"
      error-color="#d32f2f"
      color="#546e7a">
        <tab-content title="select theme" :before-change="leaveTabOne">
            <fieldset class="vue-form-generator">
                <div class="form-group required">
                    <label>Select Theme</label>
                    <ul class="collection">
                        <li class="collection-item"
                            v-for="item in themes"
                            v-on:click.stop.prevent="selectTheme(null, item.name)"
                            v-bind:class="isSelected(item.name) ? 'active' : ''">
                            <admin-components-action v-bind:model="{ command: 'selectTheme', target: item.name, title: item.name }"></admin-components-action>
                        </li>
                    </ul>
                    <div v-if="formErrors.unselectedThemeError" class="errors">
                        <span track-by="index">Selection required</span>
                    </div>
                </div>
            </fieldset>
            <p>
                This wizard allows you to create a site from an existing theme. If you'd like to create a more complex
                site please use the commandline tool `percli create project &lt;name&gt;` to create a site managed as a
                full project.
            </p>
        </tab-content>
        <tab-content title="choose name" :before-change="leaveTabTwo">
            <vue-form-generator 
              :model   ="formmodel"
              :schema  ="nameSchema"
              :options ="formOptions"
              ref      ="nameTab">
            </vue-form-generator>
        </tab-content>
        <tab-content title="verify">
            Creating Site `{{formmodel.name}}` from existing theme `{{formmodel.templatePath}}`
        </tab-content>
    </form-wizard>
</div>
</template>

<script>
    export default {
        props: ['model'],
        data:
            function() {
                return {
                    formErrors: {
                        unselectedThemeError: false
                    },
                    formmodel: {
                        path: $perAdminApp.getNodeFromView('/state/tools/pages'),
                        name: '',
                        templatePath: ''

                    },
                    formOptions: {
                        validationErrorClass: "has-error",
                        validationSuccessClass: "has-success",
                        validateAfterChanged: true,
                        focusFirstField: true
                    },
                    nameSchema: {
                      fields: [
                        {
                            type: "input",
                            inputType: "text",
                            label: "Site Name",
                            model: "name",
                            required: true,
                            validator: this.nameAvailable
                        }
                      ]
                    }
                }

        },
        created: function() {
            //By default select the first item in the list;
            this.selectTheme(this, this.themes[0].name);
        },
        computed: {
            pageSchema: function() {
            },
            themes: function() {
                const themes = $perAdminApp.findNodeFromPath($perAdminApp.getView().admin.nodes, '/content/sites').children
                const siteRootParts = this.formmodel.path.split('/').slice(0,4)
                return themes.filter( (item) => item.name.startsWith('theme'))
            }
        },
        methods: {
            selectTheme: function(me, target){
                if(me === null) me = this;
                me.formmodel.templatePath = target;
                this.validateTabOne(me);
            },
            isSelected: function(target) {
                return this.formmodel.templatePath === target
            },
            onComplete: function() {
                $perAdminApp.stateAction('createSite', { fromName: this.formmodel.templatePath, toName: this.formmodel.name })
            },
            validateTabOne: function(me) {
                me.formErrors.unselectedThemeError = ('' === '' + me.formmodel.templatePath);

                return !me.formErrors.unselectedThemeError;
            },
            leaveTabOne: function() {
                if('' !== ''+this.formmodel.templatePath) {
//                    $perAdminApp.getApi().populateComponentDefinitionFromNode(this.formmodel.templatePath)
                }

                return this.validateTabOne(this);
            },
            nameAvailable(value) {
                if(!value || value.length === 0) {
                    return ['name is required']
                } else {
                    const folder = $perAdminApp.findNodeFromPath($perAdminApp.getView().admin.nodes, this.formmodel.path)
                    for(let i = 0; i < folder.children.length; i++) {
                        if(folder.children[i].name === value) {
                            return ['name aready in use']
                        }
                    }
                    return []
                }
            },
            leaveTabTwo: function() {
                return this.$refs.nameTab.validate()
            }

        }
    }
</script>
