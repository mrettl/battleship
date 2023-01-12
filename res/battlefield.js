state = {
    "action":"NOT_STARTED",
    "shipLengths":[5,4,3],
    "turn":15,
    "playerState2":{
        "unplacedShipLengths":[5,4,3],
        "board":{
            "strikes":[
                [false,false,false,false,false,false,false,false],
                [false,false,false,false,false,false,false,false],
                [false,false,false,false,false,false,false,false],
                [false,false,false,false,false,false,false,false],
                [false,false,false,false,false,false,false,false],
                [false,false,false,false,false,false,false,false],
                [false,false,false,false,false,false,false,false],
                [false,false,false,false,false,false,false,false]
            ],
            "safetyZones":[
                [false,false,false,false,false,false,false,false],
                [false,false,false,true,true,false,false,false],
                [false,false,true,true,true,true,false,false],
                [true,true,true,true,true,false,false,false],
                [true,true,true,true,false,true,true,false],
                [true,true,true,true,true,true,true,true],
                [true,true,true,true,false,true,true,true],
                [true,true,true,true,true,true,true,true]
            ],
            "placedShips":[
                [false,false,false,false,false,false,false,false],
                [false,false,false,false,false,false,false,false],
                [false,false,false,true,true,false,false,false],
                [false,false,true,false,false,false,false,false],
                [true,false,true,false,false,false,false,false],
                [true,false,true,false,false,true,true,false],
                [true,false,true,false,false,false,false,false],
                [true,false,true,false,false,true,true,true]
            ]
        }
    },
    "playerState1":{
        "unplacedShipLengths":[5,4,3,2,2],
        "board":{
            "strikes":[
                [true,false,false,false,false,false,false,false],
                [false,false,false,false,false,false,false,false],
                [false,false,false,false,false,false,false,false],
                [false,false,false,false,false,false,false,false],
                [false,false,false,false,false,false,false,false],
                [false,false,false,false,false,false,false,false],
                [false,false,false,false,false,false,false,false],
                [false,false,false,false,false,false,false,false]
            ],
            "safetyZones":[
                [false,false,false,false,false,false,false,false],
                [false,false,false,false,false,false,false,false],
                [false,false,false,false,false,false,false,false],
                [false,false,false,false,false,false,false,false],
                [false,false,false,false,false,false,false,false],
                [false,false,false,false,false,false,false,false],
                [false,false,false,false,false,false,false,false],
                [false,false,false,false,false,false,false,false]
            ],
            "placedShips":[
                [false,false,false,false,false,false,false,false],
                [false,false,false,false,false,false,false,false],
                [false,false,false,false,false,false,false,false],
                [false,false,false,false,false,false,false,false],
                [false,false,false,false,false,false,false,false],
                [false,false,false,false,false,false,false,false],
                [false,false,false,false,false,false,false,false],
                [false,false,false,false,false,false,false,false]
            ]
        }
    }
}

localState = {
    "shipRotations": [],
    "isPlayer1": true
}

function get_enemy_field(row, column) {
    return document.querySelector("#enemy-board tr:nth-child(" + (row+2) + ") td:nth-child(" + (column+2) + ")")
}

function get_friendly_field(row, column) {
    return document.querySelector("#friendly-board tr:nth-child(" + (row+2) + ") td:nth-child(" + (column+2) + ")")
}

function setPlayer1() {
    checkbox = document.getElementById("is-player1")
    checkbox.checked = !checkbox.checked;
    localState["isPlayer1"] = checkbox.checked;
}

function addShipButton() {
    var shipLength = document.getElementById("new-ship-length").valueAsNumber
    addShip(shipLength);
}

function addShip(shipLength) {
    if([1, 2, 3, 4, 5, 6, 7, 8].findIndex(v => v == shipLength) > -1) {
        state['shipLengths'].push(shipLength)

        const httpRequest = new XMLHttpRequest()
        httpRequest.onreadystatechange = onResponse;
        httpRequest.overrideMimeType("text/plain")
        httpRequest.open("POST", "/app?shipLengths", true)
        httpRequest.send(JSON.stringify(state['shipLengths']))
    }
}

function removeShipButton(obj) {
    entry = obj.parentElement;

    shipLength = Number(obj.parentElement.getElementsByTagName("div")[0].innerText);
    removeShip(shipLength);
}

function removeShip(shipLength) {
    index = state["shipLengths"].findIndex(v => v == shipLength)
    if(index > -1) {
        state["shipLengths"].splice(index, 1)
        
        const httpRequest = new XMLHttpRequest()
        httpRequest.onreadystatechange = onResponse;
        httpRequest.overrideMimeType("text/plain")
        httpRequest.open("POST", "/app?shipLengths", true)
        httpRequest.send(JSON.stringify(state['shipLengths']))
    }

    update();
}

function resetButton() {
    const httpRequest = new XMLHttpRequest()
    httpRequest.onreadystatechange = onResponse;
    httpRequest.overrideMimeType("text/plain")
    httpRequest.open("POST", "/app?reset", true)
    httpRequest.send("")
}

function startButton() {
    const httpRequest = new XMLHttpRequest()
    httpRequest.onreadystatechange = onResponse;
    httpRequest.overrideMimeType("text/plain")
    httpRequest.open("POST", "/app?start", true)
    httpRequest.send(JSON.stringify({
        "isPlayer1": localState["isPlayer1"]
    }))
}

function onResponse(obj) {
    httpRequest = obj.target;
    if (httpRequest.readyState === XMLHttpRequest.DONE && 
        httpRequest.status == 200) {
        // Typical action to be performed when the document is ready:
        state = JSON.parse(httpRequest.responseText)
        update()
    }
}

function requestUpdate() {
    const httpRequest = new XMLHttpRequest()
    httpRequest.onreadystatechange = onResponse;
    httpRequest.overrideMimeType("text/plain")
    httpRequest.open("POST", "/app", true)  // use GET with no-cache?
    httpRequest.send("")
}

function update() {
    show_enemy_ship = false;
    function enemy_field_onclick(row, col) {}

    function allowDrop(ev) {}
    function drag(ev) {}
    function drop(ev) {}

    if(localState["isPlayer1"]) {
        myPlayerStateName = "playerState1"
        opositePlayerStateName = "playerState2"
        playerName = "PLAYER_1"
        oponentName = "PLAYER_2"
    } else {
        myPlayerStateName = "playerState2"
        opositePlayerStateName = "playerState1"
        playerName = "PLAYER_2"
        oponentName = "PLAYER_1"
    }

    // update action
    action = state["action"]
    if(action == "NOT_STARTED") {
        document.getElementById("overlay").hidden = false
        document.getElementById("win").hidden = true
        document.getElementById("loose").hidden = true
        document.getElementById("new-game").hidden = false
        document.getElementById("user-prompt").innerText = "add ships and start game"
        document.getElementById("turn").innerText = ""

    } else if(action == "PLACE_SHIPS" || action == "PLACE_SHIPS_POLICY") {
        document.getElementById("overlay").hidden = true
        document.getElementById("user-prompt").innerText = "drag & drop ships onto the friendly board"
        document.getElementById("turn").innerText = ""

        function allowDrop(ev) {
            ev.preventDefault();
        }
        function drag(ev) {
            ev.dataTransfer.setData("text", ev.target.id);
        }
        function drop(ev) {
            ev.preventDefault();
            var data = ev.dataTransfer.getData("text");
        
            ship = document.getElementById(data);
            ship_id = Number(ship.id.split("-")[1])
            localState["shipRotations"].splice(ship_id, 1)

            length = ship.getElementsByTagName("td")["length"]
            rows = ship.getElementsByTagName("tr")["length"]
        
            tokens = ev.target.id.split("-")
            row = Number.parseInt(tokens[1])
            col = Number.parseInt(tokens[2])
            
            if(rows == 1) {
                startRow = row;
                startColumn = Math.min(col, 8-length)
        
            } else {
                startRow = Math.min(row, 8-length)
                startColumn = col;
            }
            
            const httpRequest = new XMLHttpRequest()
            httpRequest.onreadystatechange = onResponse;
            httpRequest.overrideMimeType("text/plain")
            httpRequest.open("POST", "/app?placeShip", true)
            httpRequest.send(JSON.stringify({
                "horizontal": rows == 1,
                "startRow": startRow,
				"startColumn": startColumn,
				"length": length
            }))
        }

    } else if(action == playerName + "_TURN") {
        document.getElementById("overlay").hidden = true
        document.getElementById("user-prompt").innerText = "click on the enemy board to detect enemy ships"
        document.getElementById("turn").innerText = "turn " + state["turn"]
        
        function enemy_field_onclick(row, col) {
            const httpRequest = new XMLHttpRequest()
            httpRequest.onreadystatechange = onResponse;
            httpRequest.overrideMimeType("text/plain")
            httpRequest.open("POST", "/app?strike", true)
            httpRequest.send(JSON.stringify([row, col]))
        }

    } else if(action == "PLAYER_1_TURN_END" || action == oponentName + "_TURN" || action == "PLAYER_2_TURN_END") {
        document.getElementById("overlay").hidden = false
        document.getElementById("win").hidden = true
        document.getElementById("loose").hidden = true
        document.getElementById("new-game").hidden = true
        document.getElementById("user-prompt").innerText = "wait"
        document.getElementById("turn").innerText = "turn " + state["turn"]

    } else if(action == playerName + "_WIN") {
        document.getElementById("overlay").hidden = false
        document.getElementById("win").hidden = false
        document.getElementById("loose").hidden = true
        document.getElementById("new-game").hidden = false
        document.getElementById("user-prompt").innerText = "add ships and start game"
        document.getElementById("turn").innerText = "turn " + state["turn"]
        
        show_enemy_ship = true;

    } else if(action == oponentName + "_WIN") {
        document.getElementById("overlay").hidden = false
        document.getElementById("win").hidden = true
        document.getElementById("loose").hidden = false
        document.getElementById("new-game").hidden = false
        document.getElementById("user-prompt").innerText = "add ships and start game"
        document.getElementById("turn").innerText = "turn " + state["turn"]

        show_enemy_ship = true;
    }

    // update shiplengths
    document.getElementById("ship-list").innerHTML = state['shipLengths']
    .map(shipLength => 
        '<li class="w3-bar">' +
        '<span onclick="removeShipButton(this)" class="w3-bar-item w3-button w3-white w3-xlarge w3-right">×</span>' +
        '<div class="w3-bar-item">' +
        '<span class="w3-large">' + shipLength + '</span><br>' +
        '</div>' +
        '</li>'
    ).join("")
    
    // update ships
    if(state["shipLengths"].length == 0) {
        document.getElementById("start-game").disabled = true; 
    } else {
        document.getElementById("start-game").disabled = false; 
    }

    unplacedShipLengths = state[myPlayerStateName]["unplacedShipLengths"]
    shipRotations = localState["shipRotations"]
    shipHtml = "";
    for(let i = 0; i < unplacedShipLengths.length; i++) {
        if(shipRotations.length <= i)
            shipRotations.push(false)

        shipLength = unplacedShipLengths[i]
        shipHtml += '<table id="ship-' + i + '" class="board w3-grey w3-hover-shadow w3-card ship" draggable="true">'
        if(shipRotations[i]) {
            shipHtml += "<tr><td> </td></tr>".repeat(shipLength);
        } else {
            shipHtml += "<tr>" + ("<td> </td>".repeat(shipLength)) + "</tr>";
        }

        shipHtml += '</table>'
    }
    document.getElementById("ships").innerHTML = shipHtml;

    document.querySelectorAll(".ship").forEach(function(ship) {
        ship.ondragstart = drag;
        ship.onclick = function() {
            index = Number(ship.id.split("-")[1])
            length = ship.getElementsByTagName("td")["length"]
            rows = ship.getElementsByTagName("tr")["length"]
            shipRotations[index] = !shipRotations[index]

            if(shipRotations[index]) {
                ship.innerHTML = "<tr><td> </td></tr>".repeat(length);
            } else {
                ship.innerHTML = "<tr>" + ("<td> </td>".repeat(length)) + "</tr>";
            }
        };
    });

    // update enemy board
    function get_enemy_field_class_name(is_ship, is_strike) {
        if(is_ship && is_strike) {
            return "w3-red"
        } else if(show_enemy_ship && is_ship) {
            return "w3-black"
        } else if(is_strike) {
            return "w3-blue"
        } else {
            return null;
        }
    }
    ships2 = state[opositePlayerStateName]["board"]["placedShips"]
    strikes2 = state[opositePlayerStateName]["board"]["strikes"]

    for(let row = 0; row < 8; row++) {
        for(let col = 0; col < 8; col++) {
            const field = get_enemy_field(row, col);
            const r = row;
            const c = col;

            classes = field.classList
            for(let i = 0; i < classes.length; i++) {
                field.classList.remove(classes[i])
            }
            class_name = get_enemy_field_class_name(ships2[row][col], strikes2[row][col]);
            if(class_name != null) {
                field.classList.add(class_name)
            }
            
            field.onclick = () => {enemy_field_onclick(r, c)};
            field.id = "enemy-" + row + "-" + col
        }
    }

    // update friendly board
    function get_friendly_field_class_name(is_ship, is_strike) {
        if(is_ship && is_strike) {
            return "w3-red"
        } else if(is_ship) {
            return "w3-black"
        } else if(is_strike) {
            return "w3-blue"
        } else {
            return null;
        }
    }
    ships1 = state[myPlayerStateName]["board"]["placedShips"]
    strikes1 = state[myPlayerStateName]["board"]["strikes"]

    for(let row = 0; row < 8; row++) {
        for(let col = 0; col < 8; col++) {
            const field = get_friendly_field(row, col);
            field.id = "friendly-" + row + "-" + col
            field.ondrop = drop;
            field.ondragover = allowDrop;

            classes = field.classList
            for(let i = 0; i < classes.length; i++) {
                field.classList.remove(classes[i])
            }
            class_name = get_friendly_field_class_name(ships1[row][col], strikes1[row][col]);
            if(class_name != null) {
                field.classList.add(class_name)
            }


        }
    }
}
