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
    $("#globalUserSearch").on("input", function () {
        const query = $(this).val().trim();
        const currentEmail = $("meta[name='userEmail']").attr("content");

        if (query.length < 4) {
            $("#globalUserResults").empty();
            return;
        }

        $.get(`/api/user/find?email=${encodeURIComponent(query)}`, function (users) {
            const html = users
                .filter(user => user.email !== currentEmail)
                .map(user => `<div class="search-user" data-user-id="${user.id}" data-user-email="${user.email}">${user.email}</div>`)
                .join("");

            $("#globalUserResults").html(html);
        });
    });

    $(document).on("click", ".search-user", function () {
        const targetId = $(this).data("user-id");
        const targetEmailVal = $(this).data("user-email");
        const currentUserId = $("meta[name='userId']").attr("content");

        let existingChat = null;

        $(".user-btn").each(function () {
            const thisEmail = $(this).data("user-email");
            if (thisEmail === targetEmailVal) {
                existingChat = $(this);
                return false;
            }
        });

        if (existingChat) {
            existingChat.trigger("click");
        } else {
            $.ajax({
                url: `/api/chat/create`,
                type: "POST",
                contentType: "application/json",
                data: JSON.stringify({currentUserId: currentUserId, targetUserId: targetId}),
                success: function (newChat) {
                    const chatHtml = `
                    <div class="user-btn unseen" data-chat-id="${newChat.id}" data-user-email="${targetEmailVal}" data-last-timestamp="0">
                        <div class="chat-email">${targetEmailVal}</div>
                        <div class="last-message" style="display: none;">
                            <div class="last-sender"></div>
                            <div class="last-content"></div>
                        </div>
                    </div>
                `;
                    $("#chatList").prepend(chatHtml);
                    $(`.user-btn[data-chat-id="${newChat.id}"]`).trigger("click");
                }
            });
        }

        $("#globalUserSearch").val("");
        $("#globalUserResults").empty();
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
    stompClient.subscribe(`/user/queue/inbox`, (msg) => {
        const message = JSON.parse(msg.body);
        const chatId = message.chatId;

        const chatDiv = $(`.user-btn[data-chat-id="${chatId}"]`);
        if (chatDiv.length) {
            updateLastMessageInChatPreview(message);
        } else {
            $.get(`/api/chat/get?id=${chatId}`, function (chat) {
                const currentUserEmail = $("meta[name='userEmail']").attr("content");
                const targetUser = (chat.user1.email === currentUserEmail) ? chat.user2 : chat.user1;
                const targetEmailVal = targetUser.email;

                const chatHtml = `
                    <div class="user-btn unseen" data-chat-id="${chat.id}" data-user-email="${targetEmailVal}" data-last-timestamp="0">
                        <div class="chat-email">${targetEmailVal}</div>
                        <div class="last-message" style="display: none;">
                            <div class="last-sender"></div>
                            <div class="last-content"></div>
                        </div>
                    </div>
                `;
                $("#chatList").prepend(chatHtml);
                updateLastMessageInChatPreview(message);
            });
        }
    });
}


function updateLastMessageInChatPreview(message) {
    const chatDiv = $(`.user-btn[data-chat-id="${message.chatId}"]`);
    if (!chatDiv.length) return;

    const currentUserEmail = $("meta[name='userEmail']").attr("content");
    const isUnseen = message.status === "UNSEEN";
    const isOwnMessage = message.sendersEmail === currentUserEmail;

    chatDiv.find(".last-message .last-sender").text(message.sendersEmail);
    chatDiv.find(".last-message .last-content").text(message.content);
    chatDiv.attr("data-last-timestamp", message.timestamp);

    if (isUnseen && !isOwnMessage) {
        chatDiv.addClass("unseen");
    } else {
        chatDiv.removeClass("unseen");
    }

    if (!chatDiv.hasClass("active")) {
        chatDiv.find(".last-message").css("display", "block");
    }

    moveChatToTop(chatDiv);
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

        if (Array.isArray(message)) {
            message.forEach(id => {
                const $msg = $(`.message[data-id='${id}']`);
                $msg.data("seen", true);
                if ($msg.data("own")) {
                    $msg.find(".status-label").text("SEEN");
                }
            });
            const chatDiv = $(`.user-btn[data-chat-id="${chatId}"]`);
            if (chatDiv.length) {

                chatDiv.removeClass("unseen");
            }
            return;
        }
        showMessageAtBottom(message);
        updateLastMessageInChatPreview(message);
        observeSeenMessages();
        scrollToBottom();
    });
}


function sendMessage() {
    const text = $("#msgInput").val().trim();
    if (!text || !currentChatId) return;

    stompClient.publish({
        destination: "/app/i",
        body: JSON.stringify({content: text, chatId: currentChatId, getterEmail: targetEmail})
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
    const seen = msg.status === "SEEN";

    return `
        <div class="message ${sideClass}" 
             data-id="${msg.id}" 
             data-seen="${seen}" 
             data-own="${isOwnMessage}">
            <div>
                <strong>${timestamp} ${msg.sendersEmail}</strong>
                ${isOwnMessage ? `<span class="status-label">${msg.status}</span>` : ``}
            </div>
            <div>${msg.content}</div>
        </div>
    `;
}


function loadInitialHistory() {
    $.get(`/api/chat/get-history?chatId=${currentChatId}`, function (data) {
        (data.content || data).forEach(showMessage);
        observeSeenMessages();
        scrollToFirstUnseen();
    });
}

function markMessagesAsSeen(ids) {
    if (!ids.length) return;

    $.ajax({
        url: "/api/chat/mark-seen",
        type: "POST",
        contentType: "application/json",
        data: JSON.stringify({chatId: currentChatId, messageIds: ids}),
        success: function () {
            ids.forEach(id => {
                const $msg = $(`.message[data-id='${id}']`);
                $msg.data("seen", true);
                if ($msg.data("own")) {
                    $msg.find(".status-label").text("SEEN");
                }
            });
        }
    });
}

function observeSeenMessages() {
    const observer = new IntersectionObserver((entries) => {
        const seenMessageIds = [];

        entries.forEach(entry => {
            if (entry.isIntersecting) {
                const $msg = $(entry.target);
                const messageId = $msg.data("id");
                const isSeen = $msg.data("seen");
                const isOwn = $msg.data("own");

                if (!isSeen && !isOwn) {
                    seenMessageIds.push(messageId);
                    $msg.data("seen", true);
                }
            }
        });

        if (seenMessageIds.length > 0) {
            markMessagesAsSeen(seenMessageIds);
        }
    }, {
        root: document.querySelector('#messages'),
        threshold: 0.3
    });


    $(".message").each(function () {
        observer.observe(this);
    });
}

function moveChatToTop(chatDiv) {
    const timestamp = Number(chatDiv.attr("data-last-timestamp") || 0);
    const $chatList = $("#chatList");

    chatDiv.detach();

    const $chats = $chatList.children(".user-btn");

    let inserted = false;
    $chats.each(function () {
        const otherTs = Number($(this).attr("data-last-timestamp") || 0);
        if (timestamp >= otherTs) {
            $(this).before(chatDiv);
            inserted = true;
            return false;
        }
    });

    if (!inserted) {
        $chatList.prepend(chatDiv);
    }
}


function scrollToFirstUnseen() {
    const $firstUnseen = $(".message").filter(function () {
        const $this = $(this);
        return !$this.data("seen") && !$this.data("own");
    }).first();

    if ($firstUnseen.length) {
        $firstUnseen[0].scrollIntoView({behavior: 'auto', block: 'center'});
    } else {
        scrollToBottom();
    }
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
