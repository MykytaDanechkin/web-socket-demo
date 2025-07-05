let stompClient = null;
let currentSubscription = null;
let currentChatId = null;
// let currentUserId = null;

$(function () {
    $(".user-btn").click(function () {
        const targetUserId = $(this).data("user-id");
        $.post("/api/check-chat", {
            userId: targetUserId,
            // userId2: targetUserId
        }, function (chatId) {
            connectToChat(chatId);
        });
    });

    $("#sendBtn").click(() => sendMessage());
    $("#msgInput").keypress((e) => {
        if (e.which === 13) sendMessage();
    });

    //currentUserId = $("meta[name='user-id']").attr("content");
});

function connectToChat(chatId) {
    if (currentSubscription) currentSubscription.unsubscribe();
    if (!stompClient || !stompClient.connected) {
        stompClient = new StompJs.Client({
            webSocketFactory: () => new SockJS('/ws'),
            onConnect: () => subscribeToChat(chatId)
        });
        stompClient.activate();
    } else {
        subscribeToChat(chatId);
    }
}

function subscribeToChat(chatId) {
    currentChatId = chatId;
    $("#messages").empty();
    currentSubscription = stompClient.subscribe(`/topic/chat/${chatId}`, (msg) => {
        const message = JSON.parse(msg.body);
        showMessage(message.content);
    });
}

function sendMessage() {
    const text = $("#msgInput").val();
    if (!text.trim() || !currentChatId) return;
    stompClient.publish({
        destination: `/app/i`,
        body: JSON.stringify({content: text, chatId: currentChatId})
    });
    $("#msgInput").val("");
}

function showMessage(text) {
    $("#messages").append(`<div>${text}</div>`);
}
