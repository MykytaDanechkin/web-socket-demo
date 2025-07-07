let stompClient = null;
let currentSubscription = null;
let currentChatId = null;

$(function () {
    $(document).on("click", ".user-btn", function () {
        $(".user-btn").removeClass("active");
        $(this).addClass("active");

        const targetUserId = $(this).data("user-id");
        $.post("/api/check-chat", { userId: targetUserId }, function (chatId) {
            connectToChat(chatId);
        });
    });

    $("#sendBtn").click(() => sendMessage());
    $("#msgInput").keypress((e) => {
        if (e.which === 13) sendMessage();
    });
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
        showMessage(message);
    });
}

function sendMessage() {
    const text = $("#msgInput").val();
    if (!text.trim() || !currentChatId) return;
    stompClient.publish({
        destination: `/app/i`,
        body: JSON.stringify({ content: text, chatId:currentChatId })
    });
    $("#msgInput").val("");
}

function showMessage(msg) {
    const currentUserEmail = $("meta[name='userEmail']").attr("content");
    const isOwnMessage = msg.sendersEmail === currentUserEmail;
    const sideClass = isOwnMessage ? 'right' : 'left';
    const color = isOwnMessage ? '#a855f7' : '#000000';
    const timestamp = new Date(msg.timestamp).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });

    const html = `
        <div style="text-align: ${sideClass}; margin: 5px 0;">
            <div style="display: inline-block; background: ${color}; color: white; padding: 10px; border-radius: 10px; max-width: 70%;">
                <div><strong>${timestamp} ${msg.sendersEmail}</strong></div>
                <br>
                <div>${msg.content}</div>
            </div>
        </div>
    `;
    $("#messages").append(html);
}


function formatTime(timestamp) {
    const date = new Date(timestamp);
    const hours = String(date.getHours()).padStart(2, '0');
    const mins = String(date.getMinutes()).padStart(2, '0');
    return `${hours}:${mins}`;
}
