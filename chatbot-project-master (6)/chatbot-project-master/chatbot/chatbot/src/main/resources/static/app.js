var stompClient = null;
var currentRoomId = null;

function setConnected(connected) {
    $("#send").prop("disabled", !connected || !currentRoomId);
    if (connected) {
        $("#conversation").show();
    } else {
        $("#conversation").hide();
    }
    $("#msg").html("");
}

function connect() {
    var socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function (frame) {
        setConnected(true);
        console.log('Connected: ' + frame);
        stompClient.subscribe('/topic/public', function (message) {
            showMessage(message.body);
        });
        loadChatRooms();
    });
}

function disconnect() {
    if (stompClient !== null) {
        stompClient.disconnect();
    }
    setConnected(false);
    console.log("Disconnected");
}

function sendMessage() {
    let message = $("#msg").val();
    if (!currentRoomId) {
        alert("Please select a chat room first");
        return;
    }

    let chatMessage = {
        message: message,
        chatRoomId: currentRoomId
    };

    console.log("Sending message: ", chatMessage);
    stompClient.send("/app/sendMessage", {}, JSON.stringify(chatMessage));

    // 보낸 메시지를 바로 화면에 표시
    let htmlContent = "<tr><td>Me: " + message + "</td></tr>";
    $("#communicate").append(htmlContent);
    $("#chat-box").scrollTop($("#chat-box")[0].scrollHeight); // 스크롤을 맨 아래로 이동

    $("#msg").val(''); // 메시지 입력 후 입력 필드 비우기
}

function showMessage(message) {
    try {
        let response = JSON.parse(message);
        let description = response.description;
        let imageUrl = response.imageUrl || "";
        let url = response.url || "";

        let htmlContent = "<tr><td>Bot: " + description + "</td></tr>";

        if (imageUrl) {
            htmlContent += "<tr><td><img src='" + imageUrl + "' alt='Image' /></td></tr>";
        }
        if (url) {
            // URL에서 백슬래시 제거
            url = url.replace(/\\/g, "");
            htmlContent += "<tr><td><a href='" + url + "' target='_blank'>" + url + "</a></td></tr>";
        }

        $("#communicate").append(htmlContent);
        $("#chat-box").scrollTop($("#chat-box")[0].scrollHeight); // 스크롤을 맨 아래로 이동
    } catch (e) {
        // JSON 파싱에 실패한 경우, 단순히 텍스트로 출력
        $("#communicate").append("<tr><td>Bot: " + message + "</td></tr>");
        $("#chat-box").scrollTop($("#chat-box")[0].scrollHeight); // 스크롤을 맨 아래로 이동
    }
}

function createRoom() {
    let roomName = $("#roomName").val();
    if (!roomName) {
        alert("Please enter a room name");
        return;
    }

    $.ajax({
        url: '/api/chat_rooms',
        type: 'POST',
        contentType: 'application/json',
        data: JSON.stringify({ chatTitle: roomName, userId: 'user1', contextUser: [], deleteYn: false }),
        success: function (data) {
            alert("Room created: " + data.chatTitle);
            loadChatRooms();
        },
        error: function (error) {
            alert("Error creating room: " + error.responseText);
        }
    });
}

function loadChatRooms() {
    $.ajax({
        url: '/api/chat_rooms',
        type: 'GET',
        success: function (data) {
            $("#chatRooms").empty();
            data.reverse().forEach(function (room) { // 새로운 채팅방이 위로 가도록 reverse() 사용
                $("#chatRooms").append(`
                    <li class="list-group-item d-flex justify-content-between align-items-center" data-id="${room.objectId}">
                        <span class="room-title">${room.chatTitle}</span>
                        <span class="show-delete-icon">
                            <i class="glyphicon glyphicon-trash"></i>
                        </span>
                        <span class="delete-icon" style="display: none;">
                            <button class="btn btn-danger btn-sm delete-room-btn" data-id="${room.objectId}">삭제</button>
                        </span>
                    </li>
                `);
            });

            // 채팅방을 클릭했을 때 이벤트 처리
            $(".list-group-item").off('click').on('click', function() {
                selectChatRoom($(this).data('id'));
                $(".list-group-item").removeClass('active');
                $(this).addClass('active');
            });

            // 삭제 아이콘을 클릭했을 때 이벤트 처리
            $(".show-delete-icon").off('click').on('click', function(e) {
                e.stopPropagation();
                $(this).hide();
                $(this).siblings('.delete-icon').show();
            });

            // 삭제 버튼을 클릭했을 때 이벤트 처리
            $(".delete-room-btn").off('click').on('click', function(e) {
                e.stopPropagation();
                deleteChatRoom($(this).data('id'));
            });
        }
    });
}

function selectChatRoom(roomId) {
    if (stompClient !== null) {
        stompClient.disconnect();
        setConnected(false);
    }
    currentRoomId = roomId;
    $("#send").prop("disabled", !currentRoomId);
    if (currentRoomId) {
        loadChatRoomMessages(currentRoomId);
        connect(); // 채팅방 선택 시 웹소켓 연결
    }
}

function loadChatRoomMessages(chatRoomId) {
    $.ajax({
        url: '/api/chat_rooms/' + chatRoomId + '/contextUsers',
        type: 'GET',
        success: function (data) {
            $("#communicate").empty();
            data.forEach(function (message) {
                let htmlContent = "<tr><td>Me: " + message.question + "</td></tr>";
                htmlContent += "<tr><td>Bot: " + message.answer + "</td></tr>";
                $("#communicate").append(htmlContent);
                $("#chat-box").scrollTop($("#chat-box")[0].scrollHeight); // 스크롤을 맨 아래로 이동
            });
        },
        error: function (error) {
            alert("Error loading messages: " + error.responseText);
        }
    });
}

function deleteChatRoom(roomId) {
    if (!roomId) {
        alert("Please select a chat room first");
        return;
    }

    $.ajax({
        url: '/api/chat_rooms/' + roomId,
        type: 'DELETE',
        success: function () {
            alert("Room deleted");
            loadChatRooms();
            $("#communicate").empty();
        },
        error: function (error) {
            alert("Error deleting room: " + error.responseText);
        }
    });
}

$(function () {
    $("form").on('submit', function (e) {
        e.preventDefault();
        sendMessage();
    });
    $("#send").off('click').on('click', function() { sendMessage(); });
    $("#createRoom").off('click').on('click', function() { createRoom(); });

    // 페이지 로드 시 채팅방 목록 불러오기
    loadChatRooms();
});
