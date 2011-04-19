<% long now = System.currentTimeMillis(); %>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta http-equiv='cache-control' content='no-cache'>
    <meta http-equiv='expires' content='0'>
    <meta http-equiv='pragma' content='no-cache'>
	<link rel="stylesheet" href="css/main.css" type="text/css" />

	<!-- Common libraries -->
    <script type="text/javascript" src="../scripts/jquery.min.js"></script>
    <script type="text/javascript" src="../scripts/jquery-ui.custom.min.js"></script>
    <script type="text/javascript" src="../scripts/date.js"></script>
    <script type="text/javascript" src="../scripts/jquery.cookies.js"></script>
    <script type="text/javascript" src="../scripts/jquery.timers.js"></script>
    <script type="text/javascript" src="../scripts/jquery.md5.js"></script>

    <!-- cloud.com scripts -->
	<script type="text/javascript" src="scripts/cloudkit.js?t=<%=now%>"></script>
	<script type="text/javascript" src="scripts/cloudkit.hosts.js?t=<%=now%>"></script>
	
	<!-- Favicon -->
	<link rel="shortcut icon" href="favicon.ico" type="image/x-icon" />

    <title>CloudKit</title>
</head>
<body>
	<div id="main" style="display:none">
    	<div id="dialogoverlay" style="display:none;">
            <div class="overlay_black"></div>
            
			<!-- Statitics overlay starts here-->
            <div class="overlay_dialogbox" style="display:block;">
            	<div class="overlay_dialogbox_top"></div>
                <div class="overlay_dialogbox_mid">
                    <div class="overlay_dialogbox_contentarea">
                        <h2>Statistics</h2>
                        <div class="overlay_dialogbox_content">
                            <div class="db_stats_gridbox">
                                <div class="db_stats_gridrow ">
                                    <div class="db_stats_gridcolumns" style="width:48%;">
                                        <div class="db_stats_gridcelltitles">Total CPU: </div>
                                    </div>
                                    <div class="db_stats_gridcolumns" style="width:50%;">
                                        <div class="db_stats_gridcelltitles"><strong>4 x 2.40 GHZ</strong></div>
                                    </div>
                                </div>
                                
                                <div class="db_stats_gridrow ">
                                    <div class="db_stats_gridcolumns" style="width:48%;">
                                        <div class="db_stats_gridcelltitles">CPU Utilized: </div>
                                    </div>
                                    <div class="db_stats_gridcolumns" style="width:50%;">
                                        <div class="db_stats_gridcelltitles"><strong>0.04%</strong></div>
                                    </div>
                                </div>
                                
                                <div class="db_stats_gridrow ">
                                    <div class="db_stats_gridcolumns" style="width:48%;">
                                        <div class="db_stats_gridcelltitles">CPU Allocated for VMs:</div>
                                    </div>
                                    <div class="db_stats_gridcolumns" style="width:50%;">
                                        <div class="db_stats_gridcelltitles"><strong>20.83%</strong></div>
                                    </div>
                                </div>
                                
                                <div class="db_stats_gridrow ">
                                    <div class="db_stats_gridcolumns" style="width:48%;">
                                        <div class="db_stats_gridcelltitles">Memory Total:</div>
                                    </div>
                                    <div class="db_stats_gridcolumns" style="width:50%;">
                                        <div class="db_stats_gridcelltitles"><strong>3.09 GB</strong></div>
                                    </div>
                                </div>
                                
                                <div class="db_stats_gridrow ">
                                    <div class="db_stats_gridcolumns" style="width:48%;">
                                        <div class="db_stats_gridcelltitles">Memory Allocated:</div>
                                    </div>
                                    <div class="db_stats_gridcolumns" style="width:50%;">
                                        <div class="db_stats_gridcelltitles"><strong>2.63 GB</strong></div>
                                    </div>
                                </div>
                                
                                <div class="db_stats_gridrow ">
                                    <div class="db_stats_gridcolumns" style="width:48%;">
                                        <div class="db_stats_gridcelltitles">Memory Used:</div>
                                    </div>
                                    <div class="db_stats_gridcolumns" style="width:50%;">
                                        <div class="db_stats_gridcelltitles"><strong>2.63 GB</strong></div>
                                    </div>
                                </div>
                                
                                <div class="db_stats_gridrow ">
                                    <div class="db_stats_gridcolumns" style="width:48%;">
                                        <div class="db_stats_gridcelltitles">Network Read:</div>
                                    </div>
                                    <div class="db_stats_gridcolumns" style="width:50%;">
                                        <div class="db_stats_gridcelltitles"><strong>4338950879.03 TB</strong></div>
                                    </div>
                                </div>
                                
                                <div class="db_stats_gridrow ">
                                    <div class="db_stats_gridcolumns" style="width:48%;">
                                        <div class="db_stats_gridcelltitles">Network Write:</div>
                                    </div>
                                    <div class="db_stats_gridcolumns" style="width:50%;">
                                        <div class="db_stats_gridcelltitles"><strong>4352955092.25 TB</strong></div>
                                    </div>
                                </div>
                                
                                
                                
                            </div>
                        </div>
                        
                        <div class="overlay_dialogbox_confirmationbox">
                            <div class="overlay_dialogbox_confirmationbuttonbox">
                                <a href="#">Cancel</a>
                                <div class="overlay_dialog_button">OK</div>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="overlay_dialogbox_bot"></div>
            </div>
            <!-- Statitics overlay ends here-->
           
            <!-- Delete overlay starts here-->
            <div class="overlay_dialogbox" style="display:none;">
            	<div class="overlay_dialogbox_top"></div>
                <div class="overlay_dialogbox_mid">
                    <div class="overlay_dialogbox_contentarea">
                        <h2>Confirmation</h2>
                        <div class="overlay_dialogbox_content">
                            <p>Please confirm that you want to delete this Host.</p>
                        </div>
                        
                        <div class="overlay_dialogbox_confirmationbox">
                            <div class="overlay_dialogbox_confirmationbuttonbox">
                                <a href="#">Cancel</a>
                                <div class="overlay_dialog_button">Confirm</div>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="overlay_dialogbox_bot"></div>
            </div>
            <!-- Delete overlay ends here-->
        </div>
    	<div id="header">
        	<div class="logo"></div>
			<div class="user_links"><p>Welcome <span id="header_username">User</span></p><p><a href="#" id="header_logout">Logout</a></p></div>
        </div>
        <div class="main_contentbg">
            <div class="db_gridcontainer">
            	<div class="db_gridcontainer_topbox">
                	<div class="db_gridcontainer_topbox_left">
                    	<h2>My Cloud</h2>
                        <div class="db_grid_searchbox">
                        	<div class="db_grid_searchicon"></div>
                            <input class="text" type="text" />
                        </div>
                    </div>
                    <div class="db_gridcontainer_topbox_right">
                    	<div class="db_grid_tabbox">
							<div class="db_grid_tabs on" id="tab_hosts">Hosts</div>
                        </div>
                        <div class="db_grid_tabbox">
                        	<div class="db_grid_tabs off" id="tab_docs">Documentation</div>
                        </div>
                    </div>
                </div>
                <div class="db_tabcontent" id="host" style="display:block;">
                    <div class="db_gridbox">
                        <div class="db_gridrows header">
                            <div class="db_gridcolumns header" style="width:20%;">
                                <div class="db_gridcelltitles header">Name</div>
                            </div>
                            <div class="db_gridcolumns header" style="width:15%;">
                              <div class="db_gridcelltitles header">State</div>
                            </div>
                            <div class="db_gridcolumns header" style="width:15%;">
                              <div class="db_gridcelltitles header">IP Address</div>
                          </div>
                            <div class="db_gridcolumns header" style="width:20%;">
                              <div class="db_gridcelltitles header">Version</div>
                          </div>
                            <div class="db_gridcolumns header" style="width:20%;">
                              <div class="db_gridcelltitles header">Last Disconnected</div>
                          </div>
                          <div class="db_gridcolumns header" style="width:10%;">
                               <div class="db_gridcelltitles header">Actions</div>
                          </div>
                      </div>
                
                      <div class="db_maingrid">
                      	<!--Reminder for completing registrtaion starts here-->
                      	<div class="db_gridmsgbox" style="display:block;">
                        	<div class="db_gridmsgbox_content">
                            	<p>
									You have successfully added your first compute node.  Please <a id="registration_complete_link" href='#'>click here</a> to complete your registration process.
								</p>
                            </div>
                            <!-- <a id="registration_complete_link" class="db_gridmsg_button" href="#">Ok</a> -->
                        </div>
                        <!--Reminder for completing registrtaion ends here-->
                       	<div class="db_gridrows">
                                <div class="db_gridcolumns" style="width:20%;">
                                    <div class="db_gridcelltitles">test8.lab.vmops.com</div>
                                </div>
                                <div class="db_gridcolumns" style="width:15%;">
                                  <div class="db_gridcelltitles green">Up</div>
                              </div>
                                <div class="db_gridcolumns" style="width:15%;">
                                  <div class="db_gridcelltitles">192.168.10.210</div>
                              </div>
                                <div class="db_gridcolumns" style="width:20%;">
                                  <div class="db_gridcelltitles">9.1.2011-01-13T21:54:37Z</div>
                              </div>
                                <div class="db_gridcolumns" style="width:20%;">
                                  <div class="db_gridcelltitles">2011-03-26T17:25:35-0700</div>
                              </div>
                                <div class="db_gridcolumns" style="width:10%;">
                                    <a class="db_statistics_icon" href="#"></a>
                                 	
                                    <a class="db_delete_icon" style="margin-left:25px" href="#"></a>
                                </div>
                           	
                         </div>
                            
                        <div class="db_gridrows">
                            <div class="db_gridcolumns" style="width:20%;">
                                <div class="db_gridcelltitles">test8.lab.vmops.com</div>
                            </div>
                            <div class="db_gridcolumns" style="width:15%;">
                                <div class="db_gridcelltitles red">Disconnected</div>
                            </div>
                            <div class="db_gridcolumns" style="width:15%;">
                                <div class="db_gridcelltitles">192.168.10.210</div>
                            </div>
                                <div class="db_gridcolumns" style="width:20%;">
                                    <div class="db_gridcelltitles">9.1.2011-01-13T21:54:37Z</div>
                                </div>
                                <div class="db_gridcolumns" style="width:20%;">
                                    <div class="db_gridcelltitles">2011-03-26T17:25:35-0700</div>
                                </div>
                                <div class="db_gridcolumns" style="width:10%;">
                                    <a class="db_statistics_icon" href="#"></a>
                                    <a class="db_delete_icon" style="margin-left:25px" href="#"></a>
                                </div>
                            </div>  
                        </div>
                        <div class="db_grid_navigationpanel">
                            <div class="db_gridb_navbox"><a href="#">Prev</a> <a href="#">Next</a> </div>
                        </div>
                  </div>
              </div>
              
              <div class="db_tabcontent" id="instructions" style="display:none;">
                    <div class="db_gridbox">
                        <div class="db_gridrows header">
                            <div class="db_gridcolumns header" style="width:70%;">
                                <div class="db_gridcelltitles header">Welcome to Cloud.com!</div>
                            </div>
                           
                      </div>
                      <div class="db_maingrid">
                            
                      	<div class="dbinstruction_contentarea">
                        	<p>Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book.</p>
                            <h3>Where should I start?</h3>
                            <p>Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum.
                            </p>      

							<h3>New to Cloud.com?</h3>
                            <p>It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum.</p>
                            
                            <p>More information:<a href="#"> Dashboard Overview</a>    |  <a href="#">  Getting Started Guide </a></p> 
                            
                            <h3>Overview</h3>
                            <p>Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum.
                            </p>
							<div class="dbinstruction_bulletbox">
                            	<ul>
                                <li> It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum.</li>
                                
                                <li>The industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged.</li>
                                </ul>
     						</div>
						</div>   
                             
                        
                        </div>
                        <div class="db_grid_navigationpanel">
                            <div class="db_gridb_navbox"><a href="#">Prev</a> <a href="#">Next</a> </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <div id="footer">
        	<div class="footer_left"><p>&copy; 2006-2011 RightScale, Inc. All rights reserved. RightScale is a registered trademark of RightScale, Inc. </p></div>
            <div class="footer_right">
            	<a class="poweredby" href="http://cloud.com/"></a>
            </div>
        </div>
    </div>
</body>
</html>
