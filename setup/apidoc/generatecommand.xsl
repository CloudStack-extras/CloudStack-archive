<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
version="1.0">
<xsl:output method="html" doctype-public="-//W3C//DTD HTML 1.0 Transitional//EN"/>
<xsl:template match="/">
<html xmlns="http://www.w3.org/1999/xhtml"><head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<link rel= "stylesheet" href="../includes/main.css" type="text/css" />
<link rel="shortcut icon" href="../favicon.ico" type="image/x-icon" />

<title>CloudStack | The Power Behind Your Cloud</title>
</head>

<body>
<div id="insidetopbg">
<div id="inside_wrapper">
	<div class="uppermenu_panel">
            <div class="uppermenu_box"><!-- #BeginLibraryItem "/libraries/uppermenu.lbi" -->

<div class="uppermenu">
       <a href="libraries/learn_download.html">Downloads</a> | <a href="libraries/company_news.html">News</a> | <a href="#">Contact Us</a> 
</div><!-- #EndLibraryItem --></div>
        </div>
        
        <div id="main_master">
            <div id="inside_header">

                <div class="header_top">
                    <a class="cloud_logo" href="index.html"></a>
                    <div class="mainemenu_panel">
                        
                    </div>
                </div>
                <div class="insideheader_bot">
                	<div class="insideheader_botleft">
                    	<h1></h1>
                    </div>

                    <div class="insideheader_botright">
                    	<div class="insideheader_button">
                        	<a class="insjoincomm_button" href="#"></a>
                            <a class="insdownload_button" href="#"></a>
                        </div>
                        <div class="insheader_buttonshadow"></div>
                    </div>
                </div>
            
            </div>

            <div id="main_content">
             	
                <div class="inside_apileftpanel">
                	<div class="inside_contentpanel" style="width:930px;">
                    	<div class="api_titlebox">
                        	<div class="api_titlebox_left">
				<xsl:for-each select="command/command">
                                <h1><xsl:value-of select="name"/></h1>
                                <span><xsl:value-of select="description"/></span>
				</xsl:for-each>
                            </div>

                            
                            <div class="api_titlebox_right">
                            	<a class="api_backbutton" href="#"></a>
                            </div>
                        </div>
                    	<div class="api_tablepanel">     
                         	<h2>Request parameters</h2>
                        	<table class="apitable">
                            	<tr class="hed">
                                	<td style="width:200px;"><strong>Parameter Name</strong></td>

                                    <td style="width:500px;">Description</td>
                                    <td style="width:180px;">Required</td>
                                </tr>
				<xsl:for-each select="command/command/request/arg">
                                <tr>
					<td style="width:200px;"><strong><xsl:value-of select="name"/></strong></td>
                                    <td style="width:500px;"><xsl:value-of select="description"/></td>
                                    <td style="width:180px;"><xsl:value-of select="required"/></td>

                                </tr>
				</xsl:for-each>
                            </table>
                        </div>
                         
                         
                         <div class="api_tablepanel">     
                         	<h2>Response Tags</h2>
                        	<table class="apitable">
                            	<tr class="hed">
                                	<td style="width:200px;"><strong>Response Name</strong></td>
                                    <td style="width:500px;">Description</td>
                                </tr>
                                
				<xsl:for-each select="command/command/response/arg">
                                <tr>
					<td style="width:200px;"><strong><xsl:value-of select="name"/></strong></td>
                                    <td style="width:500px;"><xsl:value-of select="description"/></td>
					<xsl:for-each select="./arguments/arg">
					<tr>
					<td style="width:200px;"><strong><xsl:value-of select="name"/></strong></td>
                                    	<td style="width:500px;"><xsl:value-of select="description"/></td>
					</tr>
						<xsl:for-each select="./arguments/arg">
						<tr>
						<td style="width:200px;"><strong><xsl:value-of select="name"/></strong></td>
		                            	<td style="width:500px;"><xsl:value-of select="description"/></td>
						</tr>
					</xsl:for-each>					
					</xsl:for-each>					
                                </tr>
				</xsl:for-each>
                                
                         
                                
                              
                            
                            </table>

                        </div>
                        
                        
                </div> 
                </div>
                  
     
            </div>
        </div><!-- #BeginLibraryItem "/libraries/footer.lbi" -->
<div id="footer">

        	<div id="footer_mainmaster">
            	<ul class="footer_linksbox">
                	<li><strong> Main </strong></li>
                    <li> <a href="index.html"> Home</a> </li>
                    <li> <a href="learn_whatcloud.html"> Learn</a> </li>

                    <li> <a href="products_cloudplatform.html"> Products</a> </li>
                    <li> <a href="#"> Community</a> </li>
                    <li> <a href="service_overview.html"> Services</a> </li>

                    <li> <a href="Partners_Main.html"> Partners</a> </li>
                    <li> <a href="company_about.html"> Company</a> </li>
                </ul>
                <ul class="footer_linksbox">
                	<li><strong> Sub </strong> </li>

                    <li> <a href="learn_videos.html"> Tour</a> </li>
                    <li> <a href="learn_download.html"> Downloads</a> </li>
                    <li> <a href="learn_FAQ.html"> FAQs</a> </li>

                    <li> <a href="#"> Blog</a> </li>
                    <li> <a href="#"> Contacts</a> </li>
                   
                </ul>
                <ul class="footer_linksbox">
                	<li><strong> Site Info </strong> </li>

                    <li> <a href="#"> Privacy Policy</a> </li>
                    <li> <a href="#"> Term of Use</a> </li>
                    <li> <a href="#"> Contacts</a> </li>

                </ul>
                <p>Copyright 2010 Cloud.com, Inc. All rights reserved </p>
            </div>
        </div>
        <script type="text/javascript">
var gaJsHost = (("https:" == document.location.protocol) ? "https://ssl." : "http://www.");
document.write(unescape("%3Cscript src='" + gaJsHost + "google-analytics.com/ga.js' type='text/javascript'%3E%3C/script%3E"));
</script>
<script type="text/javascript">
try {
var pageTracker = _gat._getTracker("UA-16163918-1");
pageTracker._setDomainName(".cloud.com");
pageTracker._trackPageview();
} catch(err) {}</script>

<script type="text/javascript" language="javascript">llactid=14481</script>
				<script type="text/javascript" language="javascript" src="http://t5.trackalyzer.com/trackalyze.js"></script><!-- #EndLibraryItem --><div class="clear"></div>      
  </div>
 </div>
</body>
</html>
</xsl:template>
</xsl:stylesheet>

