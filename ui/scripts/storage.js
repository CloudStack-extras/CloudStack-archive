(function(cloudStack) {

  var diskofferingObjs, selectedDiskOfferingObj;

  cloudStack.sections.storage = {
    title: 'label.storage',
    id: 'storage',
    sectionSelect: {
      label: 'label.select-view'
    },
    sections: {
      /**
       * Volumes
       */
      volumes: {
        type: 'select',
        title: 'label.volumes',
        listView: {
          id: 'volumes',
          label: 'label.volumes',
          fields: {
            name: { label: 'label.name' },
            type: { label: 'label.type' },
            storagetype: { label: 'label.storage.type' },
            vmdisplayname: { label: 'label.vm.display.name' },
            state: {
              converter: function(str) {
                // For localization
                return 'state.' + str;
              },
              label: 'State',
              indicator: { 'Ready': 'on' }
            }
          },

          // List view actions
          actions: {
            // Add volume
            add: {
              label: 'label.add.volume',

              messages: {
                confirm: function(args) {
                  return 'message.add.volume';
                },
                notification: function(args) {
                  return 'label.add.volume';
                }
              },

              createForm: {
                title: 'label.add.volume',
                desc: 'message.add.volume',
                fields: {
                  name: {
                    label: 'label.name',
                    validation: { required: true }
                  },
                  availabilityZone: {
                    label: 'label.availability.zone',
                    select: function(args) {
                      $.ajax({
                        url: createURL("listZones&available=true"),
                        dataType: "json",
                        async: true,
                        success: function(json) {
                          var items = json.listzonesresponse.zone;
                          args.response.success({descriptionField: 'name', data: items});
                        }
                      });
                    }
                  },
                  diskOffering: {
                    label: 'label.disk.offering',
                    select: function(args) {
                      $.ajax({
                        url: createURL("listDiskOfferings"),
                        dataType: "json",
                        async: false,
                        success: function(json) {
                          diskofferingObjs = json.listdiskofferingsresponse.diskoffering;
                          var items = [];
                          $(diskofferingObjs).each(function(){
                            items.push({id: this.id, description: this.displaytext});
                          });
                          args.response.success({data: items});
                        }
                      });

                      args.$select.change(function() {
                        var diskOfferingId = $(this).val();
                        $(diskofferingObjs).each(function(){
                          if(this.id == diskOfferingId) {
                            selectedDiskOfferingObj = this;
                            return false; //break the $.each() loop
                          }
                        });
                        if(selectedDiskOfferingObj == null)
                          return;

                        var $form = $(this).closest('form');
                        var $diskSize = $form.find('.form-item[rel=diskSize]');
                        if (selectedDiskOfferingObj.iscustomized == true) {
                          $diskSize.css('display', 'inline-block');
                        }
                        else {
                          $diskSize.hide();
                        }
                      });
                    }
                  }

                  ,
                  diskSize: {
                    label: 'label.disk.size.gb',
                    validation: { required: true, number: true },
                    isHidden: true
                  }

                }
              },

              action: function(args) {
                var array1 = [];
                array1.push("&name=" + args.data.name);
                array1.push("&zoneId=" + args.data.availabilityZone);
                array1.push("&diskOfferingId=" + args.data.diskOffering);

                // if(thisDialog.find("#size_container").css("display") != "none") { //wait for Brian to include $form in args
                if (selectedDiskOfferingObj.iscustomized == true) {
                  array1.push("&size=" + args.data.diskSize);
                }

                $.ajax({
                  url: createURL("createVolume" + array1.join("")),
                  dataType: "json",
                  async: true,
                  success: function(json) {
                    var jid = json.createvolumeresponse.jobid;
                    args.response.success(
                      {_custom:
                       {jobId: jid,
                        getUpdatedItem: function(json) {
                          return json.queryasyncjobresultresponse.jobresult.volume;
                        },
                        getActionFilter: function() {
                          return volumeActionfilter;
                        }
                       }
                      }
                    );
                  },
                  error: function(json) {
                    args.response.error(parseXMLHttpResponse(json));
                  }
                });
              },

              notification: {
                poll: pollAsyncJobResult
              }
            }

          },

          dataProvider: function(args) {
            var array1 = [];
            if(args.filterBy != null) {
              if(args.filterBy.search != null && args.filterBy.search.by != null && args.filterBy.search.value != null) {
                switch(args.filterBy.search.by) {
                case "name":
                  if(args.filterBy.search.value.length > 0)
                    array1.push("&keyword=" + args.filterBy.search.value);
                  break;
                }
              }
            }

            var apiCmd = "listVolumes&listAll=true&page=" + args.page + "&pagesize=" + pageSize+ array1.join("");
            if(args.context != null) {
              if("instances" in args.context) {
                apiCmd += "&virtualMachineId=" + args.context.instances[0].id;
              }
            }

            $.ajax({
              url: createURL(apiCmd),
              dataType: "json",
              async: true,
              success: function(json) {
                var items = json.listvolumesresponse.volume;
                args.response.success({
                  actionFilter: volumeActionfilter,
                  data: items
                });
              }
            });
          },

          detailView: {
            name: 'Volume details',
            viewAll: { path: 'storage.snapshots', label: 'label.snapshots' },
            actions: {
              takeSnapshot: {
                label: 'label.action.take.snapshot',
                messages: {
                  confirm: function(args) {
                    return 'message.action.take.snapshot' ;
                  },
                  notification: function(args) {
                    return 'label.action.take.snapshot';
                  }
                },
                action: function(args) {
                  $.ajax({
                    url: createURL("createSnapshot&volumeid=" + args.context.volumes[0].id),
                    dataType: "json",
                    async: true,
                    success: function(json) {
                      var jid = json.createsnapshotresponse.jobid;
                      args.response.success(
                        {_custom:
                         {
												   jobId: jid //take snapshot from a volume doesn't change any property in this volume. So, don't need to specify getUpdatedItem() to return updated volume. Besides, createSnapshot API doesn't return updated volume. 
                         }
                        }
                      );
                    }
                  });
                },
                notification: {
                  poll: pollAsyncJobResult
                }
              },

              recurringSnapshot: {
                label: 'label.snapshot.schedule',
                action: {
                  custom: cloudStack.uiCustom.recurringSnapshots({
                    desc: 'message.snapshot.schedule',
                    dataProvider: function(args) {
                      $.ajax({
                        url: createURL('listSnapshotPolicies'),
                        data: {
                          volumeid: args.context.volumes[0].id
                        },
                        async: true,
                        dataType: 'json',
                        success: function(data) {
                          args.response.success({
                            data: $.map(
                              data.listsnapshotpoliciesresponse.snapshotpolicy ? data.listsnapshotpoliciesresponse.snapshotpolicy : [],
                              function(snapshot, index) {
                                return {
                                  id: snapshot.id,
                                  type: snapshot.intervaltype,
                                  time: snapshot.intervaltype > 0 ?
                                    snapshot.schedule.split(':')[1] + ':' + snapshot.schedule.split(':')[0] :
                                    snapshot.schedule,
                                  timezone: snapshot.timezone,
                                  keep: snapshot.maxsnaps,
                                  'day-of-week': snapshot.intervaltype == 2 ?
                                    snapshot.schedule.split(':')[2] : null,
                                  'day-of-month': snapshot.intervaltype == 3 ?
                                    snapshot.schedule.split(':')[2] : null
                                };
                              }
                            )
                          });
                        }
                      });
                    },
                    actions: {
                      add: function(args) {
                        var snap = args.snapshot;

                        var data = {
                          keep: snap.maxsnaps,
                          timezone: snap.timezone
                        };

                        var convertTime = function(minute, hour, meridiem, extra) {
                          var convertedHour = meridiem == 'PM' ?
                                (hour != 12 ? parseInt(hour) + 12 : 12) : (hour != 12 ? hour : '00');
                          var time = minute + ':' + convertedHour;
                          if (extra) time += ':' + extra;

                          return time;
                        };

                        switch (snap['snapshot-type']) {
                        case 'hourly': // Hourly
                          $.extend(data, {
                            schedule: snap.schedule
                          }); break;

                        case 'daily': // Daily
                          $.extend(data, {
                            schedule: convertTime(
                              snap['time-minute'],
                              snap['time-hour'],
                              snap['time-meridiem']
                            )
                          }); break;

                        case 'weekly': // Weekly
                          $.extend(data, {
                            schedule: convertTime(
                              snap['time-minute'],
                              snap['time-hour'],
                              snap['time-meridiem'],
                              snap['day-of-week']
                            )
                          }); break;

                        case 'monthly': // Monthly
                          $.extend(data, {
                            schedule: convertTime(
                              snap['time-minute'],
                              snap['time-hour'],
                              snap['time-meridiem'],
                              snap['day-of-month']
                            )
                          }); break;
                        }

                        $.ajax({
                          url: createURL('createSnapshotPolicy'),
                          data: {
                            volumeid: args.context.volumes[0].id,
                            intervaltype: snap['snapshot-type'],
                            maxsnaps: snap.maxsnaps,
                            schedule: data.schedule,
                            timezone: snap.timezone
                          },
                          dataType: 'json',
                          async: true,
                          success: function(successData) {
                            var snapshot = successData.createsnapshotpolicyresponse.snapshotpolicy;

                            args.response.success({
                              data: {
                                id: snapshot.id,
                                type: snapshot.intervaltype,
                                time: snapshot.intervaltype > 0 ?
                                  snapshot.schedule.split(':')[1] + ':' + snapshot.schedule.split(':')[0] :
                                  snapshot.schedule,
                                timezone: snapshot.timezone,
                                keep: snapshot.maxsnaps,
                                'day-of-week': snapshot.intervaltype == 2 ?
                                  snapshot.schedule.split(':')[2] : null,
                                'day-of-month': snapshot.intervaltype == 3 ?
                                  snapshot.schedule.split(':')[2] : null
                              }
                            });
                          }
                        });
                      },
                      remove: function(args) {
                        $.ajax({
                          url: createURL('deleteSnapshotPolicies'),
                          data: {
                            id: args.snapshot.id
                          },
                          dataType: 'json',
                          async: true,
                          success: function(data) {
                            args.response.success();
                          }
                        });
                      }
                    },

                    // Select data
                    selects: {
                      schedule: function(args) {
                        var time = [];

                        for (var i = 1; i <= 59; i++) {
                          time.push({
                            id: i,
                            name: i
                          });
                        }

                        args.response.success({
                          data: time
                        });
                      },
                      timezone: function(args) {
                        args.response.success({
                          data: $.map(timezoneMap, function(value, key) {
                            return {
                              id: key,
                              name: value
                            };
                          })
                        });
                      },
                      'day-of-week': function(args) {
                        args.response.success({
                          data: [
                            { id: 1, name: 'label.sunday' },
                            { id: 2, name: 'label.monday' },
                            { id: 3, name: 'label.tuesday' },
                            { id: 4, name: 'label.wednesday' },
                            { id: 5, name: 'label.thursday' },
                            { id: 6, name: 'label.friday' },
                            { id: 7, name: 'label.saturday' }
                          ]
                        });
                      },

                      'day-of-month': function(args) {
                        var time = [];

                        for (var i = 1; i <= 31; i++) {
                          time.push({
                            id: i,
                            name: i
                          });
                        }

                        args.response.success({
                          data: time
                        });
                      },

                      'time-hour': function(args) {
                        var time = [];

                        for (var i = 1; i <= 12; i++) {
                          time.push({
                            id: i,
                            name: i
                          });
                        }

                        args.response.success({
                          data: time
                        });
                      },

                      'time-minute': function(args) {
                        var time = [];

                        for (var i = 0; i <= 59; i++) {
                          time.push({
                            id: i < 10 ? '0' + i : i,
                            name: i < 10 ? '0' + i : i
                          });
                        }

                        args.response.success({
                          data: time
                        });
                      },

                      'time-meridiem': function(args) {
                        args.response.success({
                          data: [
                            { id: 'AM', name: 'AM' },
                            { id: 'PM', name: 'PM' }
                          ]
                        });
                      }
                    }
                  })
                },
                messages: {
                  notification: function(args) {
                    return 'label.snapshot.schedule';
                  }
                }
              },

              attachDisk: {
                addRow: 'false',
                label: 'label.action.attach.disk',
                messages: {
                  confirm: function(args) {
                    return 'Are you sure you want to attach disk?';
                  },
                  notification: function(args) {
                    return 'label.action.attach.disk';
                  }
                },
                createForm: {
                  title: 'label.action.attach.disk',
                  desc: 'label.action.attach.disk',
                  fields: {
                    virtualMachineId: {
                      label: 'label.instance',
                      select: function(args) {
                        var zoneid = args.context.volumes[0].zoneid;
                        var items = [];
                        var data;

                        if (!args.context.projects) {
                          data = {
                            zoneid: zoneid,
                            domainid: args.context.volumes[0].domainid,
                            account: args.context.volumes[0].account
                          };
                        } else {
                          data = {
                            zoneid: zoneid,
                            projectid: args.context.projects[0].id
                          };
                        }

                        $(['Running', 'Stopped']).each(function() {
                          $.ajax({
                            url: createURL('listVirtualMachines'),
                            data: $.extend(data, {
                              state: this.toString()
                            }),
                            async: false,
                            success: function(json) {
                              var instanceObjs= json.listvirtualmachinesresponse.virtualmachine;
                              $(instanceObjs).each(function() {
                                items.push({
                                  id: this.id, description: this.displayname ?
                                    this.displayname : this.name
                                });
                              });
                            }
                          });
                        });

                        args.response.success({data: items});
                      }
                    }
                  }
                },
                action: function(args) {
                  $.ajax({
                    url: createURL("attachVolume&id=" + args.context.volumes[0].id + '&virtualMachineId=' + args.data.virtualMachineId),
                    dataType: "json",
                    async: true,
                    success: function(json) {
                      var jid = json.attachvolumeresponse.jobid;
                      args.response.success(
                        {_custom:
                         {jobId: jid,
                          getUpdatedItem: function(json) {
                            return json.queryasyncjobresultresponse.jobresult.volume;
                          },
                          getActionFilter: function() {
                            return volumeActionfilter;
                          }
                         }
                        }
                      );
                    }
                  });
                },
                notification: {
                  poll: pollAsyncJobResult
                }
              },
              detachDisk: {
                label: 'label.action.detach.disk',
                messages: {
                  confirm: function(args) {
                    return 'message.detach.disk';
                  },
                  notification: function(args) {
                    return 'label.action.detach.disk';
                  }
                },
                action: function(args) {
                  $.ajax({
                    url: createURL("detachVolume&id=" + args.context.volumes[0].id),
                    dataType: "json",
                    async: true,
                    success: function(json) {
                      var jid = json.detachvolumeresponse.jobid;
                      args.response.success(
                        {_custom:
                         {jobId: jid,
                          getUpdatedItem: function(json) {
                            return {
                              virtualmachineid: null,
                              vmname: null
                            };
                          },
                          getActionFilter: function() {
                            return volumeActionfilter;
                          }
                         }
                        }
                      );
                    }
                  });
                },
                notification: {
                  poll: pollAsyncJobResult
                }
              },

              downloadVolume: {
                label: 'label.action.download.volume',
                messages: {
                  confirm: function(args) {
                    return 'message.download.volume.confirm';
                  },
                  notification: function(args) {
                    return 'label.action.download.volume';
                  },
                  complete: function(args) {
                    var url = decodeURIComponent(args.url);
                    var htmlMsg = _l('message.download.volume');
                    var htmlMsg2 = htmlMsg.replace(/#/, url).replace(/00000/, url);
                    //$infoContainer.find("#info").html(htmlMsg2);
                    return htmlMsg2;
                  }
                },
                action: function(args) {
                  $.ajax({
                    url: createURL("extractVolume&id=" + args.context.volumes[0].id + "&zoneid=" + args.context.volumes[0].zoneid + "&mode=HTTP_DOWNLOAD"),
                    dataType: "json",
                    async: true,
                    success: function(json) {
                      var jid = json.extractvolumeresponse.jobid;
                      args.response.success(
                        {_custom:
                         {jobId: jid,
                          getUpdatedItem: function(json) {
                            return json.queryasyncjobresultresponse.jobresult.volume;
                          },
                          getActionFilter: function() {
                            return volumeActionfilter;
                          }
                         }
                        }
                      );
                    }
                  });
                },
                notification: {
                  poll: pollAsyncJobResult
                }
              },

              createTemplate: {
                label: 'label.create.template',
                messages: {
                  confirm: function(args) {
                    return 'message.create.template';
                  },
                  notification: function(args) {
                    return 'label.create.template';
                  }
                },
                createForm: {
                  title: 'label.create.template',
                  desc: '',
                  fields: {
                    name: { label: 'label.name', validation: { required: true }},
                    displayText: { label: 'label.description', validation: { required: true }},
                    osTypeId: {
                      label: 'label.os.type',
                      select: function(args) {
                        $.ajax({
                          url: createURL("listOsTypes"),
                          dataType: "json",
                          async: true,
                          success: function(json) {
                            var ostypes = json.listostypesresponse.ostype;
                            var items = [];
                            $(ostypes).each(function() {
                              items.push({id: this.id, description: this.description});
                            });
                            args.response.success({data: items});
                          }
                        });
                      }
                    },
                    isPublic: { label: 'label.public', isBoolean: true },
                    isPasswordEnabled: { label: 'label.password.enabled', isBoolean: true }
                  }
                },
                action: function(args) {
                  /*
                   var isValid = true;
                   isValid &= validateString("Name", $thisDialog.find("#create_template_name"), $thisDialog.find("#create_template_name_errormsg"));
                   isValid &= validateString("Display Text", $thisDialog.find("#create_template_desc"), $thisDialog.find("#create_template_desc_errormsg"));
                   if (!isValid)
                   return;
                   $thisDialog.dialog("close");
                   */

                  var array1 = [];
                  array1.push("&name=" + todb(args.data.name));
                  array1.push("&displayText=" + todb(args.data.displayText));
                  array1.push("&osTypeId=" + args.data.osTypeId);
                  array1.push("&isPublic=" + (args.data.isPublic=="on"));
                  array1.push("&passwordEnabled=" + (args.data.isPasswordEnabled=="on"));

                  $.ajax({
                    url: createURL("createTemplate&volumeId=" + args.context.volumes[0].id + array1.join("")),
                    dataType: "json",
                    async: true,
                    success: function(json) {
                      var jid = json.createtemplateresponse.jobid;
                      args.response.success(
                        {_custom:
                         {jobId: jid,
                          getUpdatedItem: function(json) {
                            return {}; //no properties in this volume needs to be updated
                          },
                          getActionFilter: function() {
                            return volumeActionfilter;
                          }
                         }
                        }
                      );
                    }
                  });
                },
                notification: {
                  poll: pollAsyncJobResult
                }
              },

              migrateToAnotherStorage: {
                label: 'label.migrate.volume',
                messages: {
                  confirm: function(args) {
                    return 'message.migrate.volume';
                  },
                  notification: function(args) {
                    return 'label.migrate.volume';
                  }
                },
                createForm: {
                  title: 'label.migrate.volume',
                  desc: '',
                  fields: {
                    storageId: {
                      label: 'label.primary.storage',
                      validation: { required: true },
                      select: function(args) {
                        $.ajax({
                          url: createURL("listStoragePools&zoneid=" + args.context.volumes[0].zoneid),
                          dataType: "json",
                          async: true,
                          success: function(json) {
                            var pools = json.liststoragepoolsresponse.storagepool;
                            var items = [];
                            $(pools).each(function() {
                              items.push({id: this.id, description: this.name});
                            });
                            args.response.success({data: items});
                          }
                        });
                      }
                    }
                  }
                },
                action: function(args) {
                  $.ajax({
                    url: createURL("migrateVolume&storageid=" + args.data.storageId + "&volumeid=" + args.context.volumes[0].id),
                    dataType: "json",
                    async: true,
                    success: function(json) {
                      var jid = json.migratevolumeresponse.jobid;
                      args.response.success(
                        {_custom:
                         {jobId: jid,
                          getUpdatedItem: function(json) {
                            return json.queryasyncjobresultresponse.jobresult.volume;
                          },
                          getActionFilter: function() {
                            return volumeActionfilter;
                          }
                         }
                        }
                      );
                    }
                  });
                },
                notification: {
                  poll: pollAsyncJobResult
                }
              },

              remove: {
                label: 'label.action.delete.volume',
                messages: {
                  confirm: function(args) {
                    return 'message.action.delete.volume';
                  },
                  notification: function(args) {
                    return 'label.action.delete.volume';
                  }
                },
                action: function(args) {
                  $.ajax({
                    url: createURL("deleteVolume&id=" + args.context.volumes[0].id),
                    dataType: "json",
                    async: true,
                    success: function(json) {
                      args.response.success();
                    }
                  });
                },
                notification: {
                  poll: function(args) {
                    args.complete();
                  }
                }
              }
            },
            tabs: {
              details: {
                title: 'label.details',

                preFilter: function(args) {
                  var hiddenFields;
                  if(isAdmin()) {
                    hiddenFields = [];
                  }
                  else {
                    hiddenFields = ["storage"];
                  }
                  return hiddenFields;
                },

                fields: [
                  {
                    name: { label: 'label.name', isEditable: true }
                  },
                  {
                    id: { label: 'ID' },
                    zonename: { label: 'label.zone' },
                    deviceid: { label: 'label.device.id' },
                    state: { label: 'label.state' },
                    type: { label: 'label.type' },
                    storagetype: { label: 'label.storage.type' },
                    storage: { label: 'label.storage' },
                    size : {
                      label: 'Size ',
                      converter: function(args) {
                        if (args == null || args == 0)
                          return "";
                        else
                          return cloudStack.converters.convertBytes(args);
                      }
                    },
                    virtualmachineid: {
                      label: 'VM ID',
                      converter: function(args) {
                        if (args == null)
                          return "detached";
                        else
                          return args;
                      }
                    },
                    vmname: { label: 'label.vm.name' },
                    vmdisplayname: { label: 'label.vm.display.name' },
                    created: { label: 'label.created', converter: cloudStack.converters.toLocalDate },
                    domain: { label: 'label.domain' },
                    account: { label: 'label.account' }
                  }
                ],

                dataProvider: function(args) {		
								  $.ajax({
										url: createURL("listVolumes&id=" + args.context.volumes[0].id),
										dataType: "json",
										async: true,
										success: function(json) {								  
											var jsonObj = json.listvolumesresponse.volume[0];   
											args.response.success(
												{
													actionFilter: volumeActionfilter,
													data: jsonObj
												}
											);		
										}
									});								
                }
              }
            }
          }
        }
      },

      /**
       * Snapshots
       */
      snapshots: {
        type: 'select',
        title: 'label.snapshots',
        listView: {
          id: 'snapshots',
          label: 'label.snapshots',
          fields: {
            volumename: { label: 'label.volume' },
            intervaltype: { label: 'label.interval.type' },
            created: { label: 'label.created', converter: cloudStack.converters.toLocalDate },
            state: {
              converter: function(str) {
                // For localization
                return 'state.'+str;
              },
              label: 'label.state', indicator: {
                converter: function(str) {
                  return 'state.' + str;
                },
                'BackedUp': 'on', 'Destroyed': 'off'
              }
            }
          },

          dataProvider: function(args) {
            var array1 = [];
            if(args.filterBy != null) {
              if(args.filterBy.search != null && args.filterBy.search.by != null && args.filterBy.search.value != null) {
                switch(args.filterBy.search.by) {
                case "name":
                  if(args.filterBy.search.value.length > 0)
                    array1.push("&keyword=" + args.filterBy.search.value);
                  break;
                }
              }
            }

            var apiCmd = "listSnapshots&listAll=true&page=" + args.page + "&pagesize=" + pageSize + array1.join("");
            if(args.context != null) {
              if("volumes" in args.context) {
                apiCmd += "&volumeid=" + args.context.volumes[0].id;
              }
            }

            $.ajax({
              url: createURL(apiCmd),
              dataType: "json",
              async: true,
              success: function(json) {
                var items = json.listsnapshotsresponse.snapshot;
                args.response.success({
                  actionFilter: snapshotActionfilter,
                  data: items
                });
              }
            });
          },

          detailView: {
            name: 'Snapshot detail',
            actions: {
              createTemplate: {
                label: 'label.create.template',
                messages: {
                  confirm: function(args) {
                    return 'message.create.template';
                  },
                  notification: function(args) {
                    return 'label.create.template';
                  }
                },
                createForm: {
                  title: 'label.create.template',
                  desc: '',
                  fields: {
                    name: { label: 'label.name', validation: { required: true }},
                    displayText: { label: 'label.description', validation: { required: true }},
                    osTypeId: {
                      label: 'label.os.type',
                      select: function(args) {
                        $.ajax({
                          url: createURL("listOsTypes"),
                          dataType: "json",
                          async: true,
                          success: function(json) {
                            var ostypes = json.listostypesresponse.ostype;
                            var items = [];
                            $(ostypes).each(function() {
                              items.push({id: this.id, description: this.description});
                            });
                            args.response.success({data: items});
                          }
                        });
                      }
                    },
                    isPublic: { label: 'label.public', isBoolean: true },
                    isPasswordEnabled: { label: 'label.password.enabled', isBoolean: true }
                  }
                },
                action: function(args) {
                  /*
                   var isValid = true;
                   isValid &= validateString("Name", $thisDialog.find("#create_template_name"), $thisDialog.find("#create_template_name_errormsg"));
                   isValid &= validateString("Display Text", $thisDialog.find("#create_template_desc"), $thisDialog.find("#create_template_desc_errormsg"));
                   if (!isValid)
                   return;
                   $thisDialog.dialog("close");
                   */

                  var array1 = [];
                  array1.push("&name=" + todb(args.data.name));
                  array1.push("&displayText=" + todb(args.data.displayText));
                  array1.push("&osTypeId=" + args.data.osTypeId);
                  array1.push("&isPublic=" + (args.data.isPublic=="on"));
                  array1.push("&passwordEnabled=" + (args.data.isPasswordEnabled=="on"));

                  $.ajax({
                    url: createURL("createTemplate&snapshotid=" + args.context.snapshots[0].id + array1.join("")),
                    dataType: "json",
                    async: true,
                    success: function(json) {
                      var jid = json.createtemplateresponse.jobid;
                      args.response.success(
                        {_custom:
                         {jobId: jid,
                          getUpdatedItem: function(json) {
                            return {}; //nothing in this snapshot needs to be updated
                          },
                          getActionFilter: function() {
                            return snapshotActionfilter;
                          }
                         }
                        }
                      );
                    }
                  });
                },
                notification: {
                  poll: pollAsyncJobResult
                }
              },

              createVolume: {
                label: 'label.action.create.volume',
                messages: {
                  confirm: function(args) {
                    return 'Are you sure you want to create volume?';
                  },
                  notification: function(args) {
                    return 'label.action.create.volume';
                  }
                },
                createForm: {
                  title: 'label.action.create.volume',
                  desc: '',
                  fields: {
                    name: {
                      label: 'label.name',
                      validation: {
                        required: true
                      }
                    }
                  }
                },
                action: function(args) {
                  var array1 = [];
                  array1.push("&name=" + todb(args.data.name));

                  $.ajax({
                    url: createURL("createVolume&snapshotid=" + args.context.snapshots[0].id + array1.join("")),
                    dataType: "json",
                    async: true,
                    success: function(json) {
                      var jid = json.createvolumeresponse.jobid;
                      args.response.success(
                        {_custom:
                         {jobId: jid,
                          getUpdatedItem: function(json) {
                            return {}; //nothing in this snapshot needs to be updated
                          },
                          getActionFilter: function() {
                            return snapshotActionfilter;
                          }
                         }
                        }
                      );
                    }
                  });
                },
                notification: {
                  poll: pollAsyncJobResult
                }
              },

              remove: {
                label: 'label.action.delete.snapshot',
                messages: {
                  confirm: function(args) {
                    return 'message.action.delete.snapshot';
                  },
                  notification: function(args) {
                    return 'label.action.delete.snapshot';
                  }
                },
                action: function(args) {
                  $.ajax({
                    url: createURL("deleteSnapshot&id=" + args.context.snapshots[0].id),
                    dataType: "json",
                    async: true,
                    success: function(json) {
                      var jid = json.deletesnapshotresponse.jobid;
                      args.response.success(
                        {_custom:
                         {jobId: jid
                         }
                        }
                      );
                    }
                  });
                },
                notification: {
                  poll: function(args) {
                    args.complete();
                  }
                }
              }
            },
            tabs: {
              details: {
                title: 'label.details',
                fields: [
                  {
                    name: { label: 'label.name' }
                  },
                  {
                    id: { label: 'label.id' },
                    volumename: { label: 'label.volume.name' },
                    state: { label: 'label.state' },
                    intervaltype: { label: 'label.interval.type' },
                    domain: { label: 'label.domain' },
                    account: { label: 'label.account' },
                    created: { label: 'label.created', converter: cloudStack.converters.toLocalDate }
                  }
                ],

                dataProvider: function(args) {
								  $.ajax({
										url: createURL("listSnapshots&id=" + args.context.snapshots[0].id),
										dataType: "json",
										async: true,
										success: function(json) {								  
											var jsonObj = json.listsnapshotsresponse.snapshot[0];   
											args.response.success(
												{
													actionFilter: snapshotActionfilter,
													data: jsonObj
												}
											);		
										}
									});		
                }
              }
            }
          }
        }
      }
    }
  };


  var volumeActionfilter = function(args) {
    var jsonObj = args.context.item;
    var allowedActions = [];

    if (jsonObj.state == 'Destroyed') {
      return [];
    }

    if(jsonObj.hypervisor != "Ovm" && jsonObj.state == "Ready") {
      allowedActions.push("takeSnapshot");
      allowedActions.push("recurringSnapshot");
    }
    if(jsonObj.state != "Allocated") {
      if(jsonObj.vmstate == "Stopped") {
        allowedActions.push("downloadVolume");
      }
    }
    if(jsonObj.state != "Creating" && jsonObj.state != "Corrupted" && jsonObj.name != "attaching") {
      if(jsonObj.type == "ROOT") {
        if (jsonObj.vmstate == "Stopped") {
          allowedActions.push("createTemplate");
        }
      }
      else {
        if (jsonObj.virtualmachineid != null) {
          if (jsonObj.storagetype == "shared" && (jsonObj.vmstate == "Running" || jsonObj.vmstate == "Stopped" || jsonObj.vmstate == "Destroyed")) {
            allowedActions.push("detachDisk");
          }
        }
        else { // Disk not attached
          allowedActions.push("remove");
          allowedActions.push("migrateToAnotherStorage");
          if (jsonObj.storagetype == "shared") {
            allowedActions.push("attachDisk");
          }
        }
      }
    }
    return allowedActions;
  };

  var snapshotActionfilter = function(args) {
    var jsonObj = args.context.item;

    if (jsonObj.state == 'Destroyed') {
      return [];
    }

    var allowedActions = [];
    if(jsonObj.state == "BackedUp") {
      allowedActions.push("createTemplate");
      allowedActions.push("createVolume");
    }
    allowedActions.push("remove");
    return allowedActions;
  }

})(cloudStack);
