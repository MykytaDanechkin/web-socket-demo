let stompClient = null;
let currentSubscription = null;
let currentChatId = null;
let targetEmail = null;

$(function () {
    initializeInboxConnection();
    $(document).on("click", ".user-btn", function () {
        const $this = $(this);
        const chatId = $this.data("chat-id");
        targetEmail = $this.data("user-email");

        if ($this.hasClass("active")) {
            $this.removeClass("active");
            $this.find(".last-message").css("display", "block");
            disconnectFromChat();
            return;
        }

        $(".user-btn").removeClass("active").find(".last-message").css("display", "block");

        $this.addClass("active");
        $this.find(".last-message").css("display", "none");

        connectToChat(chatId);
    });


    $("#sendBtn").click(sendMessage);
    $("#msgInput").keypress(e => {
        if (e.which === 13) sendMessage();
    });
});

function initializeInboxConnection() {
    const connectCallback = () => {
        subscribeToInbox();
    };

    if (!stompClient || !stompClient.connected) {
        stompClient = new StompJs.Client({
            webSocketFactory: () => new SockJS('/ws'),
            onConnect: connectCallback,
        });
        stompClient.activate();
    } else {
        connectCallback();
    }
}

function subscribeToInbox() {
    console.log("Subscribing to /user/queue/inbox...");
    stompClient.subscribe(`/user/queue/inbox`, (msg) => {
        console.log("Inbox message received", msg);
        const message = JSON.parse(msg.body);
        updateLastMessageInChatPreview(message);
    });
}
function updateLastMessageInChatPreview(message) {
    const chatDiv = $(`.user-btn[data-chat-id="${message.chatId}"]`);
    if (chatDiv.length) {
        console.log(`Updating chat preview for chatId=${message.chatId}, status=${message.status}`);
        console.log("ChatDiv found:", chatDiv.length > 0);

        chatDiv.find(".last-message .last-sender").text(message.sendersEmail);
        chatDiv.find(".last-message .last-content").text(message.content);

        const isUnseen = message.status === "UNSEEN";

        if (isUnseen) {
            chatDiv.addClass("unseen");
        } else {
            chatDiv.removeClass("unseen");
        }

        if (!chatDiv.hasClass("active")) {
            chatDiv.find(".last-message").css("display", "block");
        }
    }
}

function connectToChat(chatId) {
    if (currentSubscription) currentSubscription.unsubscribe();
    currentChatId = chatId;
    $("#messages").empty();

    const connectCallback = () => {
        subscribeToChat(chatId);
        loadInitialHistory();
    };

    if (!stompClient || !stompClient.connected) {
        stompClient = new StompJs.Client({
            webSocketFactory: () => new SockJS('/ws'),
            onConnect: connectCallback,
        });
        stompClient.activate();
    } else {
        connectCallback();
    }
}

function disconnectFromChat() {
    if (currentSubscription) {
        currentSubscription.unsubscribe();
        currentSubscription = null;
    }
    currentChatId = null;
    $("#messages").empty();
}

function subscribeToChat(chatId) {
    currentSubscription = stompClient.subscribe(`/topic/chat/${chatId}`, (msg) => {
        const message = JSON.parse(msg.body);
        showMessageAtBottom(message);
        updateLastMessageInChatPreview(message);
        scrollToBottom();
    });
}

function sendMessage() {
    const text = $("#msgInput").val().trim();
    if (!text || !currentChatId) return;

    stompClient.publish({
        destination: "/app/i",
        body: JSON.stringify({ content: text, chatId: currentChatId, getterEmail: targetEmail })
    });
    $("#msgInput").val("");
}

function showMessage(msg) {
    const html = renderMessageHtml(msg);
    $("#messages").prepend(html);
}

function showMessageAtBottom(msg) {
    const html = renderMessageHtml(msg);
    $("#messages").append(html);
}

function renderMessageHtml(msg) {
    const currentUserEmail = $("meta[name='userEmail']").attr("content");
    const isOwnMessage = msg.sendersEmail === currentUserEmail;
    const sideClass = isOwnMessage ? 'right' : 'left';
    const timestamp = formatTime(msg.timestamp);
    return `
        <div class="message ${sideClass}">
            <div><strong>${timestamp} ${msg.sendersEmail}</strong></div>
            <div>${msg.content}</div>
        </div>
    `;
}

function loadInitialHistory() {
    $.get(`/api/chat/get-history?chatId=${currentChatId}`, function (data) {
        (data.content || data).forEach(showMessage);
        scrollToBottom();
    });
}

function formatTime(timestamp) {
    const date = new Date(timestamp);
    const hours = String(date.getHours()).padStart(2, '0');
    const mins = String(date.getMinutes()).padStart(2, '0');
    return `${hours}:${mins}`;
}

function scrollToBottom() {
    $("#messages").scrollTop($("#messages")[0].scrollHeight);
}
