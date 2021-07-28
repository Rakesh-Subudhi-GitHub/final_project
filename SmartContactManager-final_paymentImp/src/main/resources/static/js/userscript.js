console.log("this is script file");


/* Set the width of the sidebar to 250px and the left margin of the page content to 250px */
function openNav() {
  document.getElementById("mySidebar").style.width = "250px";
  document.getElementById("main").style.marginLeft = "250px";
}

/* Set the width of the sidebar to 0 and the left margin of the page content to 0 */
function closeNav() {
  document.getElementById("mySidebar").style.width = "0";
  document.getElementById("main").style.marginLeft = "0";
}

//alert("js is activated")

const search = () =>
{
  console.log("searching...");

  let query= $("#search-input").val();

  if(query=='')
  {
    $(".search-result").hide();
  }

  else{

    //search
    console.log(query);//console print

    //sending request to server

    let url=`http://localhost:8888/search/${query}`  //its not '' its this ( ` ) backtick

    fetch(url)
    .then((response) =>{
      return response.json();   //all data will search that come in json form and collect
    })
    .then((data) =>{
      
      //data result
      console.log(data);  //then all data will print in concole 
    
      //showing the result in html so convert the file as it is
      let text= `<div class= 'list-group' >`//backtick

      data.forEach((contact) => {        //data collect the all data of poticular contact  
                                                      //need one only name thats way take  here
        text += `<a href='/user/contact/${contact.cid}' class='list-group-item list-group-item-action'> ${contact.name} </a>`  //transfer to help of hreaf
      });

      text+=`</div>`

      $(".search-result").html(text);  //convert the html
      $(".search-result").show();   // then print the result  help of jquery
    });


    $(".search-result").show();
  }
};

//payment system module


//1.step => request to server to create order
const paymentStart=()=>{
console.log("payment start..Testing");

let amount=$("#payment_field").val();

console.log(amount);

if(amount==''||amount==' '|| amount==null)
{
  swal({
    title: "Failed !!",
    text: "Enter Amount Fast ..!!",
    icon: "error",
    button: "Try Again !!",
  });
  return;
}

src="https://checkout.razorpay.com/v1/checkout.js";
//code
//2.step we will use ajax to send request to server to create order


$.ajax(
  {
    url:'/user/create_order',
    data:JSON.stringify({TotalAmount:amount,info:'order_reqest'}),
    contentType:'application/json',
    type:'POST',
    dataType:'json',


    //all are correct then it success is run

    success: function (response){

      //invoked when Success
      console.log("++++++++++++++++++++++++++++");
      console.log(response);
      
     if(response.status == "created")
     //status: "created"
    {

      console.log("========================================")
        //open create payment form and show here
      let option={

          key:'rzp_test_WtE0pWsJK15cKR',
          amount: response.amount,
          currency: "INR",
          name:'Smart Contact Manager',
          description:'Donation',
          image:'',
          order_id: response.id,

          handler: function(response){
                    console.log(response.razorpay_payment_id);
                    console.log(response.razorpay_order_id);
                    console.log(response.razorpay_signature);
            
                    console.log('payment Successful');


            //create a method pass the Transaction table Updated 
            updatePaymentServer(response.razorpay_payment_id,response.razorpay_order_id,'paid');        

                    //alert("congrates !! paymemnt successfull !!");
                    // swal({
                    //   title: "Good job!",
                    //   text: "Congrates !! Paymemnt Successfull !!",
                    //   icon: "success",
                    //   button: "Done!",
                    // });

          },

        prefill: {
            name: "",
            email: "",
            contact: ""

            },

            notes: {
              address: "Rakesh project" 
            },

          theme: {
              color: "#3399cc"
              },
                 
        };


        //++++++++++++++++++++++This is Object Created++++++++++++++++++++++++++++++

         //payment option object is created (1)
         let rzp=new Razorpay(option);

        //++++++++++++++++++++++++++++++++++++++++++++++++++++

        //error somethig wrong this to run
        rzp.on('payment.failed', function (response){
          console.log(response.error.code);
          console.log(response.error.description);
          console.log(response.error.source);
          console.log(response.error.step);
          console.log(response.error.reason);
          console.log(response.error.metadata.order_id);
          console.log(response.error.metadata.payment_id);

         // alert("Opps Payment faild... Something went wrong try again..");

          swal({
            title: "Failed !!",
            text: "Opps Payment failed... Something went wrong try again..",
            icon: "error",
            button: "Try Again !!",
          });

         });

        
        //this is the main to open the page and show the pupup (2)
        //this to open when it Success
        rzp.open();


      }

    },

    error: function(error){
      //invoked when error
      //alert("Something get wrong try again");
      console.log(error);
      swal({
        title: "Failed !!",
        text: "Opps Payment failed... Something went wrong try again..",
        icon: "error",
        button: "Try Again !!",
      });
    },
    
  });

};

function updatePaymentServer(payment_id,order_id,status)
{

  console.log("Payment id : "+payment_id);
  console.log("Order id: "+order_id);
  console.log("status : "+status);

  $.ajax(
    {
      url:'/user/update_order',
      data:JSON.stringify({payment_id: payment_id,order_id: order_id,status :status}),
      contentType:'application/json',
      type:'POST',
      
      success:function(response)
      {
        console.log("Update Successful");
        console.log(response)
        console.log(response.msg)
        swal({
          title: "Good job!",
          text: "Congrates !! Paymemnt Successfull !!",
          icon: "success",
          button: "Done!",
        });

      },
      error:function(error)
      {
        console.log("Something problem");
        console.log(error);
        swal({
          title: "Failed !",
          text: "Your payment is Successful, but we did not get on server ,We will contact as soon as ..",
          icon: "error",
          button: "Done!",
        });
      }

    });
}




