//Variable for chatbox
const chatbox = document.getElementById("chatbox");

//Variable for Messages container
const messageContainer = document.getElementById("messages");

//Variable to manage the button
const buttonSend = document.getElementById("send-msg-btn");

//Variable for the input text
const messageInput = document.getElementById("input-msg");

//Grab the text inside the input text and return it
function getMessage() {
    let message = messageInput.value;

    return message;
}

//Function to create a div with id message to put it inside the messages container,
//it will contain text that was previously on the input text
function sendMessage() {
    const messageSent = document.createElement("div");
    const horizontalLine = document.createElement("hr");

    //Assigning the message value to the messageSent and changing some values to the text
    messageSent.textContent = getMessage();
    messageSent.style.padding = "5px";
    messageSent.style.fontFamily = "Comic Sans MS";
    messageSent.scrollIntoView({"behavior": "smooth"});

    messageSent.setAttribute("class", "message");

    //If the message is empty, cancel everything and dont add it to the chat
    if (messageSent.textContent == "") {
        return;
    }

    messageContainer.appendChild(messageSent);
    messageContainer.appendChild(horizontalLine);
}

//To clear the text from the input text - where the messages are written
function clearInputText() {
    messageInput.value="";
}

//Scroll to bottom
function scrollToBottom() {
   messageContainer.scrollTop = messageContainer.scrollHeight;
}

//Create an event on click that involves the button and updating the message to the div
//with id= 'messages'
buttonSend.addEventListener("click", function () {
    sendMessage();
    clearInputText();
    scrollToBottom();
})

let stompClient = null;

function connect() {
    const socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);

    stompClient.connect({}, function () {
        stompClient.subscribe('/topic/messages', function (message) {
            showMessage(message.body);
        });
    });
}

function sendMessage() {
    const message = getMessage();
    stompClient.send('/app/message', {}, message);
}

function showMessage(message) {
    const messageSent = document.createElement("div");
    const horizontalLine = document.createElement("hr");

    messageSent.textContent = message;
    messageSent.style.padding = "5px";
    messageSent.style.fontFamily = "Comic Sans MS";

    messageSent.setAttribute("class", "message");

    messageContainer.appendChild(messageSent);
    messageContainer.appendChild(horizontalLine);
    scrollToBottom();
}

// Call connect() to establish the WebSocket connection
connect();