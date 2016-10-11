#!/bin/bash
curl -v -H "$(cat headers.txt)" --include  --data-binary @$1 $2