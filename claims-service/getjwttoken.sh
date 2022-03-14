#!/bin/bash

: '
This is a convenience script that can be used to fetch a JWT token for an application registered
with Okta using the Okta using the resource owner flow. Before running the script, you must set up
the following environment variables:

OKTA_DOMAIN : The unique hostname assigned to your Okta dev account, e.g., dev-xxxxxxx.okta.com
OKTA_CLIENT_ID: Client Id of the registered application
OKTA_CLIENT_SECRET: Client secret of the registered application

Once these are set up, you can invoke the script as follows:

./getjwttoken.sh username password

If the authentication is successful, the JWT token will be printed in the output.

See https://developer.okta.com/docs/guides/implement-password/use-flow/ for more details

'

# Account for some portability issues between Linux and Mac OS
if [[ "$OSTYPE" == "linux-gnu"* ]]; then
   BASE64_CMD="base64 -w 0"
elif [[ "$OSTYPE" == "darwin"* ]]; then
   BASE64_CMD="base64"
fi

if [ "$OKTA_DOMAIN" = "" ] || [ "$OKTA_CLIENT_ID" = "" ] || [ "$OKTA_CLIENT_SECRET" = "" ]; then
  echo "Check that OKTA_DOMAIN, OKTA_CLIENT_ID and OKTA_CLIENT_SECRET are all set"
  exit
fi

if [ "$1" = "" ] | [ "$2" = "" ]; then
  echo Usage: "$0" username password
  exit
fi

CLIENT_CREDENTIALS=$(echo -n "$OKTA_CLIENT_ID":"$OKTA_CLIENT_SECRET" | $BASE64_CMD)

curl -s --request POST \
  --url https://"$OKTA_DOMAIN"/oauth2/default/v1/token \
  --header "accept: application/json" \
  --header "authorization: Basic $CLIENT_CREDENTIALS" \
  --header "content-type: application/x-www-form-urlencoded" \
  --data "grant_type=password&username=$1&password=$2&scope=openid" | jq -r .access_token
