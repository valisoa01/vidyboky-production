sudo apt-get install jq
export API_URL_SSM="`aws ssm get-parameter --name /boky-vidy-5d8d2ac5/$1/api/url`"
export API_URL=`echo $API_URL_SSM | jq -r '.Parameter.Value'`
curl --fail "$API_URL$2"