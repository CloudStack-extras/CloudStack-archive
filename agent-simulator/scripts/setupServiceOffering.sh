
  #
  #  Copyright (C) 2011 Citrix Systems, Inc.  All rights reserved
  #
  #
  # This software is licensed under the GNU General Public License v3 or later.
  #
  # It is free software: you can redistribute it and/or modify
  # it under the terms of the GNU General Public License as published by
  # the Free Software Foundation, either version 3 of the License, or any later version.
  # This program is distributed in the hope that it will be useful,
  # but WITHOUT ANY WARRANTY; without even the implied warranty of
  # MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  # GNU General Public License for more details.
  #
  # You should have received a copy of the GNU General Public License
  # along with this program.  If not, see <http://www.gnu.org/licenses/>.
  #
  #
 

x=$1

so_query="GET	http://10.91.30.226:8096/client/?command=createServiceOffering&name=SO$x&displayText=SO$x&storageType=local&cpuNumber=1&cpuSpeed=1000&memory=512&offerha=false&usevirtualnetwork=false&hosttags=SP$x	HTTP/1.0\n\n"

echo -e $so_query | nc -v -w 20 10.91.30.226 8096
