#!/usr/bin/env bash
#
# Sample usage:
#   ./test_all.bash start stop
#   start and stop are optional
#
#   HOST=localhost PORT=7000 ./test-em-all.bash
#
# When not in Docker
#: ${HOST=localhost}
#: ${PORT=7000}

# When in Docker
: ${HOST=localhost}
: ${PORT=8080}

#array to hold all our test data ids
allTestLoanIds=()
allTestBookIds=()
allTestLibraryWorkerIds=()
allTestCustomerIds=()

function assertCurl() {

  local expectedHttpCode=$1
  local curlCmd="$2 -w \"%{http_code}\""
  local result=$(eval $curlCmd)
  local httpCode="${result:(-3)}"
  RESPONSE='' && (( ${#result} > 3 )) && RESPONSE="${result%???}"

  if [ "$httpCode" = "$expectedHttpCode" ]
  then
    if [ "$httpCode" = "200" ]
    then
      echo "Test OK (HTTP Code: $httpCode)"
    else
      echo "Test OK (HTTP Code: $httpCode, $RESPONSE)"
    fi
  else
      echo  "Test FAILED, EXPECTED HTTP Code: $expectedHttpCode, GOT: $httpCode, WILL ABORT!"
      echo  "- Failing command: $curlCmd"
      echo  "- Response Body: $RESPONSE"
      exit 1
  fi
}

function assertEqual() {

  local expected=$1
  local actual=$2

  if [ "$actual" = "$expected" ]
  then
    echo "Test OK (actual value: $actual)"
  else
    echo "Test FAILED, EXPECTED VALUE: $expected, ACTUAL VALUE: $actual, WILL ABORT"
    exit 1
  fi
}

#have all the microservices come up yet?
function testUrl() {
    url=$@
    if curl $url -ks -f -o /dev/null
    then
          echo "Ok"
          return 0
    else
          echo -n "not yet"
          return 1
    fi;
}

#prepare the test data that will be passed in the curl commands for posts and puts
function setupTestdata() {

#CREATE SOME CUSTOMER TEST DATA - THIS WILL BE USED FOR THE POST REQUEST
body=\
'{
    "firstname": "Nathan",
    "lastname": "Roos",
    "phoneNumber": "222-222-222-222",
    "email": "nathan.roos@example.com"
}'
    recreateCustomerAggregate 1 "$body"

#CREATE SOME EMPLOYEE TEST DATA - THIS WILL BE USED FOR THE POST REQUEST
body=\
'{
   "firstname": "Jeremy",
    "lastname": "Roos",
    "email": "jeremy.roos@gmail.com",
    "booksLoaned": 1,
    "libraryWorkerAddress": {
    "streetNumber": "7039",
    "streetName": "Rue Marie-Rollet",
    "city": "Montreal",
    "province": "QUEBEC",
    "postalCode": "H8N3C6"
    },
    "position": {
        "positionTitle": "ASSISTANT"
    },
    "librarianPhoneNumber": {
        "phoneNumber": "555-1234",
        "type": "WORK"
    }
}'
    recreateEmployeeAggregate 1 "$body"


#CREATE SOME INVENTORY TEST DATA - THIS WILL BE USED FOR THE POST REQUEST
body=\
'{
  "firstname": "jeremey",
  "lastname": "pllll",
  "genre": "FICTION",
  "title": "yellow",
  "author": "Berm",
  "copiesAvailable": 2
}'
    recreateInventoryAggregate 1 "$body"




#CREATE SOME PURCHASE REQUEST TEST DATA - THIS WILL BE USED FOR THE POST REQUEST
#all use customerId cc9c2c7f-afc9-46fb-8119-17158e54d02f

body=\
'{
  "accountId": "84a8ec6e-2fdc-4c6d-940f-9b2274d44420",
  "librarianId": "2b3c4d5e-e29b-41d4-a716-446655440001",
  "bookId": "660e8400-e29b-41d4-a716-446655440001",
  "loanStatus": "ACTIVE",
  "loanDate": "2025-05-12T00:00:00.000+00:00",
  "dueDate": "2025-06-12T00:00:00.000+00:00"
}'
    recreatePurchaseAggregate 1 "$body" "84a8ec6e-2fdc-4c6d-940f-9b2274d44420"



} #end setupTestdata


#USING EMPLOYEE TEST DATA - EXECUTE POST REQUEST
function recreateEmployeeAggregate() {
    local testId=$1
    local aggregate=$2

    #create the employee aggregates and record the generated employeeIds
    librarianId=$(curl -X POST http://$HOST:$PORT/api/v1/workers -H "Content-Type:
    application/json" --data "$aggregate" | jq '.librarianId')
    allTestLibraryWorkerIds[$testId]=$librarianId
    echo "Added workers Aggregate with librarianId: ${allTestLibraryWorkerIds[$testId]}"
}

function recreateCustomerAggregate() {
    local testId=$1
    local aggregate=$2

    #create the customer aggregate and record the generated customerIds
    accountId=$(curl -X POST http://$HOST:$PORT/api/v1/accounts -H "Content-Type:
    application/json" --data "$aggregate" | jq '.accountId')
    allTestCustomerIds[$testId]=$accountId
    echo "Added Customer Aggregate with accountId: ${allTestCustomerIds[$testId]}"
}

#USING PURCHASE TEST DATA - EXECUTE POST REQUEST
function recreatePurchaseAggregate() {
    local testId=$1
    local aggregate=$2
    local accountId=$3

    #create the purchase aggregates and record the generated purchaseIds
    loanId=$(curl -X POST http://$HOST:$PORT/api/v1/accounts/${accountId}/loans -H "Content-Type:
    application/json" --data "$aggregate" | jq '.loanId')
    allTestLoanIds[$testId]=$loanId
    echo "Added Loan Aggregate with loanId: ${allTestLoanIds[$testId]}"
}

#USING INVENTORY TEST DATA - EXECUTE POST REQUEST
function recreateInventoryAggregate() {
    local testId=$1
    local aggregate=$2

    #create the inventory aggregates and record the generated inventoryIds
    bookId=$(curl -X POST http://$HOST:$PORT/api/v1/books -H "Content-Type:
    application/json" --data "$aggregate" | jq '.bookId')
    allTestBookIds[$testId]=$bookId
    echo "Added Book Aggregate with inventoryId: ${allTestBookIds[$testId]}"
}



#don't start testing until all the microservices are up and running
function waitForService() {
    url=$@
    echo -n "Wait for: $url... "
    n=0
    until testUrl $url
    do
        n=$((n + 1))
        if [[ $n == 100 ]]
        then
            echo " Give up"
            exit 1
        else
            sleep 6
            echo -n ", retry #$n "
        fi
    done
}

#start of test script
set -e

echo "HOST=${HOST}"
echo "PORT=${PORT}"

if [[ $@ == *"start"* ]]
then
    echo "Restarting the test environment..."
    echo "$ docker-compose down"
    docker-compose down
    echo "$ docker-compose up -d"
    docker-compose up -d
fi

#try to delete an entity/aggregate that you've set up but that you don't need. This will confirm that things are working
#I've set up an inventory with no vehicles in it
#waitForService curl -X DELETE http://$HOST:$PORT/api/v1/inventories/7890abcd-ef12-3456-7890-abcdef123456

#try to delete a resource that you won't be needing. This will confirm that our microservices architecture is up and running.
waitForService curl -X DELETE http://$HOST:$PORT/api/v1/workers/1a2b3c4d-e29b-41d4-a716-446655440000


setupTestdata

#EXECUTE EXPLICIT TESTS AND VALIDATE RESPONSES
#
#EMPLOYEES
#
##verify that a get all employees works
echo -e "\nTest 1: Verify that a GET ALL workers works"
assertCurl 200 "curl http://$HOST:$PORT/api/v1/workers -s"
assertEqual 10 $(echo $RESPONSE | jq ". | length")
#Note: data-mysql.sql adds 6, we deleted 1 during the init phase, and then we posted 1, so now we have 6.
#
#
## Verify that a normal get by id of earlier posted employee works
echo -e "\nTest 2: Verify that a normal GET by librarianId of earlier posted worker works"
assertCurl 200 "curl http://$HOST:$PORT/api/v1/workers/${allTestLibraryWorkerIds[1]} -s"
assertEqual ${allTestLibraryWorkerIds[1]} $(echo $RESPONSE | jq .librarianId)
assertEqual "\"Jeremy\"" $(echo $RESPONSE | jq ".firstname")
#
#
# Verify that a 404 (Not Found) HTTP Status is returned for a GET by employeeId with a non existing employeeId (c3540a89-cb47-4c96-888e-ff96708db4d7)
echo -e "\nTest 3: Verify that a 404 (Not Found) HTTP Status is returned for a GET by librarianId request with a non existing librarianId"
assertCurl 404 "curl http://$HOST:$PORT/api/v1/workers/2b3c4d5e-e29b-41d4-a716-446655440002 -s"
#
#
# Verify that a 422 (Unprocessable Entity) HTTP Status is returned for a GET by employeeId with an invalid (c3540a89-cb47-4c96-888e-ff9670)
echo -e "\nTest 4: Verify that a 422 (Unprocessable Entity) HTTP Status for a GET by librarianId request with an invalid librarianId"
assertCurl 422 "curl http://$HOST:$PORT/api/v1/workers/2b3c4d5e-e29b-41d4-a716-44665544000 -s"
#
#
## Verify that the post of an employee works
echo -e "\nTest 5: Verify that an post of an employee works"
body=\
'{
    "firstname": "Jeremy",
    "lastname": "Roos",
    "email": "jeremy.roos@gmail.com",
    "booksLoaned": 1,
    "libraryWorkerAddress": {
    "streetNumber": "7039",
    "streetName": "Rue Marie-Rollet",
    "city": "Montreal",
    "province": "QUEBEC",
    "postalCode": "H8N3C6"
    },
    "position": {
        "positionTitle": "ASSISTANT"
    },
    "librarianPhoneNumber": {
        "phoneNumber": "555-1234",
        "type": "WORK"
    }
}'
assertCurl 201 "curl -X POST http://$HOST:$PORT/api/v1/workers -H \"Content-Type: application/json\" -d '${body}' -s"
assertEqual "\"Jeremy\"" $(echo $RESPONSE | jq ".firstname")
assertEqual "\"Roos\"" $(echo $RESPONSE | jq ".lastname")
#
#
## Verify that an update of an earlier posted employee works
echo -e "\nTest 6: Verify that an update of an earlier posted employee works"
body=\
'{
   "firstname": "Nathan",
    "lastname": "Roos",
    "email": "nathan.roos@example.com",
    "booksLoaned": null,
    "libraryWorkerAddress": {
        "streetNumber": "7039",
        "streetName": "Rue Marie-Rollet",
        "city": "Montreal",
        "province": "QUEBEC",
        "postalCode": "H8N3C6"
    },
    "position": {
        "positionTitle": "LIBRARY_CLERK"
    },
    "librarianPhoneNumber": {
        "phoneNumber": "555-1234",
        "type": "MOBILE"
    },
    "links": []
}'
assertCurl 200 "curl -X PUT http://$HOST:$PORT/api/v1/workers/${allTestLibraryWorkerIds[1]} -H \"Content-Type: application/json\" -d '${body}' -s"
assertEqual ${allTestLibraryWorkerIds[1]} $(echo $RESPONSE | jq .librarianId)
assertEqual "\"Nathan\"" $(echo $RESPONSE | jq ".firstname")
assertEqual "\"Roos\"" $(echo $RESPONSE | jq ".lastname")
#
#
# Verify that a 404 (Not Found) HTTP Status is returned for a PUT with a non existing employeeId (c3540a89-cb47-4c96-888e-ff96708db4d7)
echo -e "\nTest 7: Verify that a 404 (Not Found) HTTP Status is returned for a PUT request with a non existing librarianId"
assertCurl 404 "curl -X PUT http://$HOST:$PORT/api/v1/workers/c3540a89-cb47-4c96-888e-ff96708db4d7 -H \"Content-Type: application/json\" -d '${body}' -s"
#
#
# Verify that a 422 (Unprocessable Entity) HTTP Status is returned for a PUT with an employeeId that is invalid (c3540a89-cb47-4c96-888e-ff9670)
echo -e "\nTest 8: Verify that a 422 (Unprocessable Entity) HTTP Status for a PUT request with an invalid librarianId"
assertCurl 422 "curl -X PUT http://$HOST:$PORT/api/v1/workers/b3c4d5e-e29b-41d4-a716-44665544000 -H \"Content-Type: application/json\" -d '${body}' -s"
#
#
# Verify that a delete of an earlier posted employee works
echo -e "\nTest 9: Verify that the delete of earlier posted worker works"
assertCurl 204 "curl -X DELETE http://$HOST:$PORT/api/v1/workers/${allTestEmployeeIds[1]} -s"
#
#
# Verify that 404 (Not Found) HTTP Status is returned for a DELETE with a non existing employeeId (c3540a89-cb47-4c96-888e-ff96708db4d7)
echo -e "\nTest 10: Verify that that a 404 (Not Found) HTTP Status is returned for a DELETE request with a non existing librarianId"
assertCurl 404 "curl -X DELETE http://$HOST:$PORT/api/v1/workers/cb3c4d5e-e29b-41d4-a716-446655440002 -s"
#
#
# Verify that a 422 (Unprocessable Entity) HTTP Status is returned for a DELETE with an employeeId that is invalid (c3540a89-cb47-4c96-888e-ff9670)
echo -e "\nTest 11: Verify that a 422 (Unprocessable Entity) HTTP Status for a DELETE request with an invalid librarianId"
assertCurl 422 "curl -X DELETE http://$HOST:$PORT/api/v1/workers/b3c4d5e-e29b-41d4-a716-44665544000 -H \"Content-Type: application/json\" -d '${body}' -s"
#
#
##INVENTORIES
#
##verify that a get all inventories works

#
#
## Verify that a normal get by id of earlier posted inventory works

#
#
# Verify that 404 (Not Found) HTTP Status is returned for a GET with a non existing inventoryId (c3540a89-cb47-4c96-888e-ff96708db4d7)

#
#
# Verify that a 422 (Unprocessable Entity) HTTP Status is returned for a GET with an inventoryID that is invalid (c3540a89-cb47-4c96-888e-ff9670)

#
#
##CUSTOMERS
#
#
##verify that a GET all customers works

#
#
## Verify that a normal get by id of earlier posted customer works

#
#
# Verify that 404 (Not Found) HTTP Status is returned for a GET with a non existing customerId (c3540a89-cb47-4c96-888e-ff96708db4d7)

#
#
# Verify that a 422 (Unprocessable Entity) HTTP Status is returned for a GET with an customerId that is invalid (c3540a89-cb47-4c96-888e-ff9670)

#
#
# Verify post of a customer

#
#
##CUSTOMER PURCHASES
#
#
##verify that a GET all customer PURCHASES works
echo -e "\nTest 21: Verify that a GET all customer loans works"
assertCurl 200 "curl http://$HOST:$PORT/api/v1/accounts/84a8ec6e-2fdc-4c6d-940f-9b2274d44420/loans -s"
assertEqual 1 $(echo $RESPONSE | jq ". | length")
#
#
## Verify that a normal GET by id of earlier posted purchase works
echo -e "\nTest 22: Verify that a normal get by id of earlier posted loan works"
assertCurl 200 "curl http://$HOST:$PORT/api/v1/accounts/84a8ec6e-2fdc-4c6d-940f-9b2274d44420/loans/${allTestLoanIds[1]} -s"
assertEqual ${allTestLoanIds[1]} $(echo $RESPONSE | jq .loanId)
assertEqual "\"Pen\"" $(echo $RESPONSE | jq ".customer_firstname")
#
#
# Verify that a 404 (Not Found) error is returned for a GET purchase request with a non-existing customerId (c3540a89-cb47-4c96-888e-ff96708db4d7)
echo -e "\nTest 23: Verify that a 404 (Not Found) HTTP Status is returned for a get purchase request with a non existing customerId"
assertCurl 404 "curl http://$HOST:$PORT/api/v1/accounts/84a8ec6e-2fdc-4c6d-940f-9b2274d44420/loans/${allTestLoanIds[1]} -s"
#
#
# Verify that a 422 (Unprocessable Entity) HTTP Status is returned for a GET purchase with an customerId that is invalid (c3540a89-cb47-4c96-888e-ff9670)
echo -e "\nTest 24 Verify that a 422 (Unprocessable Entity) HTTP Status for a GET purchase request with an invalid customerId"
assertCurl 422 "curl http://$HOST:$PORT/api/v1/accounts/84a8ec6e-2fdc-4c6d-940f-9b2274d4442/loans/${allTestLoanIds[1]} -s"
#
#
# Verify that a 404 (Not Found) error is returned for a GET purchase request with a non-existing purchaseId (c3540a89-cb47-4c96-888e-ff96708db4d7)
echo -e "\nTest 25: Verify that a 404 (Not Found) HTTP Status is returned for a get purchase request purchaseId a non existing purchaseId"
assertCurl 404 "curl http://$HOST:$PORT/api/v1/accounts/84a8ec6e-2fdc-4c6d-940f-9b2274d44420/loans/c3540a89-cb47-4c96-888e-ff96708db4d7 -s"
#
#
# Verify that a 422 (Unprocessable Entity) HTTP Status is returned for a GET purchase with an purchaseId that is invalid (c3540a89-cb47-4c96-888e-ff9670)
echo -e "\nTest 26: Verify that a 422 (Unprocessable Entity) HTTP Status for a GET purchase request with an invalid customerId"
assertCurl 422 "curl http://$HOST:$PORT/api/v1/customers/84a8ec6e-2fdc-4c6d-940f-9b2274d44420/purchases/c3540a89-cb47-4c96-888e-ff9670 -s"
#
#
# Verify that aggregate invariant worked for the previous purchase post and that the vehicle status was changed to SALE_PENDING
# make sure it was AVAILABLE in the test data
#echo -e "\nTest 27: AGGREGATE INVARIANT: Verify using a vehicle get by inventoryId and vin that the vehicle status was updated upon post of the customer purchase"
#assertCurl 200 "curl http://$HOST:$PORT/api/v1/inventories/3fe5c169-c1ef-42ea-9e5e-870f30ba9dd0/vehicles/5YJ3E1EA7KF654321 -s"
#assertEqual "\"SALE_PENDING\"" $(echo $RESPONSE | jq ".status")
#
#
#Verify that a PUT for a previous purchase post works. Change the purchase status to PURCHASE_COMPLETED, then verify vehicle status is SOLD
#
#
#Verify that a DELETE for a previous post works. Verify that the status of the sale is now PURCHASE_CANCELLED and that the vehicle status is AVAILABLE
#
#


#cleanup docker

if [[ $@ == *"stop"* ]]
then
    echo "We are done, stopping the test environment..."
    echo "$ docker-compose down"
    docker-compose down
fi