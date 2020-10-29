#!/bin/bash

# In Ubuntu inotifywait is provided by the inotify-tools package
inotifywait -m /usr/eclipse/dropins/plugins -e create  |
    while read dir action file; do
        echo "The file '$file' appeared in directory '$dir' via '$action'"
        # do something with the file
        if [ "$action" == "CREATE" ]
        then
          echo "Moving file $file one directory up"
          mv $dir$file "$dir/.."
        fi
    done
