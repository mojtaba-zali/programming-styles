// Additional references:
// https://stackoverflow.com/questions/4351521/how-do-i-pass-command-line-arguments-to-a-node-js-program
// https://nodejs.org/docs/latest/api/globals.html
// https://thisdavej.com/using-ini-files-in-your-node-js-applications-for-health-and-profit/

// The plugins folder is always ./plugins
const PLUGINS_FOLDER=__dirname+'/plugins'
// The plugins configuration file is always ./config.ini
const PLUGINS_CONFIGURATION=__dirname+'/config.ini'
// The name of the configuration value that specified which plugin to use is: pluginName
const PLUGIN_NAME_KEY='pluginName'

///////// GLOBAL VARIABLES SET BY HOLLYWOOD-STYLE CODE IN END-EVENT
///////// The following classes handle the game logic and emply these global variables/lists to process inputs and etc.
///////// Then, these lists are utilized for rendering UI, using plugigns.
let evaluatedInputList = []; 
let boardPositionsList = [];
let gameStatusList = [];

class GameFramework {

    constructor() {
	this._loadEventHandlers = [];
        this._doWorkEventHandlers = [];
        this._endEventHandlers = []; // GLOBAL VARIABL SET IN END-EVENT
    }

    registerForLoadEvent(handler) {
        this._loadEventHandlers.push(handler);
    }

    registerForDoWorkEvent(handler) {
        this._doWorkEventHandlers.push(handler);
    }

    registerForEndEvent(handler) {
        this._endEventHandlers.push(handler);
    }

    run(args) {
        this._loadEventHandlers.forEach(handler => {
            handler(args);
        }); 
        this._doWorkEventHandlers.forEach(handler => {
            handler();
        }); 
        this._endEventHandlers.forEach(handler => {
            handler();
        }); 
    }
}

class InputEvaluationManager {
    
    constructor(framework, args) {
        this._evaluatedInputs = [];
        this._boardEventHandlers = [];
        this._sequenceInput = args;
        
        framework.registerForLoadEvent(this._inputEvaluation.bind(this));
        framework.registerForDoWorkEvent(this._boardPositions.bind(this))
        framework.registerForEndEvent(this._retrunEvaluatedList.bind(this)) // SET GLOBAL VARIABLES
    }

    _inputEvaluation(args) {
        this._evaluatedInputs[0] = false; // default value - not invalid
        for (let i = 0; i <= args.length; i++) {
            this._evaluatedInputs[i+1] = this._isInvalid(args, i);
        }
    }

    _isInvalid(args, round) {
        if (args[round] != "1" && args[round] != "2" && args[round] != "3" && args[round] != "4" && args[round] != "5" && args[round] != "6" & round < args.length)
            return true;
        return false;
    }

    _boardPositions() {
        for (let i = 0; i <= this._sequenceInput.length; i++) {
            this._boardEventHandlers.forEach(handler => {
                handler(this._sequenceInput[i], this._evaluatedInputs[i+1], i);
            });
        }
    }  
    
    registerBoardHandler(handler) {
        this._boardEventHandlers.push(handler);
    }

    _retrunEvaluatedList() {
        evaluatedInputList = this._evaluatedInputs;
    }
}

class BoardManager {
    
    constructor(framework, inputEvaluationManager, args) {
        inputEvaluationManager.registerBoardHandler(this._updateBoardPositions.bind(this));
        framework.registerForDoWorkEvent(this._updateGameStatus.bind(this))
        framework.registerForEndEvent(this._returnBoardPositionsList.bind(this)) // SET GLOBAL VARIABLES
        this._sequenceInput = args;
        this._boardPositions = [];
        this._boardPositions[0] = [5, 5, 5, 5, 11];
        this._gameStatusEventHandlers = [];
    }

    _updateBoardPositions(currentInput, isInvalid, index) {
        let currentBoardPositions = this._boardPositions[index].slice();
        if (isInvalid) {
            this._boardPositions.push(currentBoardPositions);
            return;
        }
        currentBoardPositions = this._updateCurrentBoardPositions(currentInput, currentBoardPositions);
        this._boardPositions.push(currentBoardPositions);
    }

    _updateCurrentBoardPositions(dice, currentBoardPositions) {
        const BOAT_POSITION = -1;
        const BOARD_INDEX = 4;
        // if 1/6 is drawn, the hunter boat moves OR after the hunter capture a fish, when the number to the fish is drawn
        if (dice == 1 || dice == 6 || currentBoardPositions[dice - 2] == BOAT_POSITION) {
            for (let i = 0; i < currentBoardPositions.length; i++) {
                if (currentBoardPositions[i] != BOAT_POSITION)
                    currentBoardPositions[i] -= 1;
            }
        }
        // If the number to the fish is received, then another fish can move by one step. CLOSEST TO SAFETY + SMALLEST NUMBER
        else if (currentBoardPositions[dice - 2] == currentBoardPositions[BOARD_INDEX]) {
            let minIndex = -1;
            for (let i = 3; i >= 0 ; i--) {
                if (currentBoardPositions[i] == currentBoardPositions[BOARD_INDEX])
                    continue;
                else if (minIndex == -1)
                    minIndex = i;
                else if (currentBoardPositions[i] >= currentBoardPositions[minIndex])
                    minIndex = i;
            }
            currentBoardPositions[minIndex] += 1;
        }
        else {
            currentBoardPositions[dice - 2] += 1;
        }
        return currentBoardPositions;
    }

    _updateGameStatus() {
        for (let i = 0; i <= this._sequenceInput.length+1; i++) {
            this._gameStatusEventHandlers.forEach(handler => {
                handler(this._boardPositions[i], i, this._sequenceInput.length);
            });
        }
    }  
    
    registerGameStatusHandler(handler) {
        this._gameStatusEventHandlers.push(handler);
    }

    _returnBoardPositionsList() {
        boardPositionsList = this._boardPositions;
    }
}

class GameStatusManager {

    constructor(framework, boardManager) {
        this._gameStatuses = [];
        boardManager.registerGameStatusHandler(this._updateGameStatus.bind(this));
        framework.registerForEndEvent(this._returnGameStatusList.bind(this)) // SET GLOBAL VARIABLES
    }

    _updateGameStatus(currentBoardPositions, round, inputsLength) {
        const BOAT_POSITION = -1;
        const BOARD_INDEX = 4;

        let freeFish = 0;
        let caughtFish = 0;
        for (let i = 0; i < currentBoardPositions.length - 1; i++) {
            if (currentBoardPositions[i] == BOAT_POSITION)
                caughtFish += 1;
            else if (currentBoardPositions[i] == currentBoardPositions[BOARD_INDEX])
                freeFish += 1;
        }
        if (round > inputsLength)
            this._gameStatuses.push("missingInput");
        else if (freeFish + caughtFish < 4)
            this._gameStatuses.push("gameContinue"); // STILL FISH IN RIVER
        else if (freeFish >= 3) 
            this._gameStatuses.push("fishesWin");
        else if (caughtFish >= 3) 
            this._gameStatuses.push("huntersWin");
        else if (freeFish == 2 && caughtFish == 2)
            this._gameStatuses.push("tie");
    }

    _returnGameStatusList() {
        gameStatusList = this._gameStatuses;
    }
}

var main = function(args) {

    //////////////// CHECK CONFIG FILE AND FETCH THE VALUE OF PLUGIN
    let pluginNameValue;
    const fs = require('fs');
    if (!fs.existsSync(PLUGINS_CONFIGURATION))
        throw new Error("config.ini is missing");
        
    const lines = fs.readFileSync(PLUGINS_CONFIGURATION, "utf-8").split(/\r?\n/);
    if (lines.length == 0) 
        throw new Error("config.ini is empty");

    lines.forEach(function(line){
        if (!line.startsWith(";")) {
            const words = line.split(/[ =]+/);
            words.forEach(function(word){
                if (word === PLUGIN_NAME_KEY)
                pluginNameValue = line.substring(line.search("=")+1); 
            })
        }                  
    })  
    if (!pluginNameValue)
        throw new Error("config.ini does not declare the required \"pluginName\" option");

    ////////////// HOLLYWOOD-STYLE ENTITIES
    let gameFramework = new GameFramework();
    let inputEvaluationManager = new InputEvaluationManager(gameFramework, args);
    let boardManager = new BoardManager(gameFramework, inputEvaluationManager, args);
    let gameStatusManager = new GameStatusManager(gameFramework, boardManager);
    gameFramework.run(args);

    //////////////// LOAD PLUGINS
    const BoardCreationManager = require(PLUGINS_FOLDER + "/" + pluginNameValue)

    let boardCreationManager = new BoardCreationManager(args, evaluatedInputList, boardPositionsList, gameStatusList);   
    boardCreationManager.print();
}

if (require.main === module) {
    var args = process.argv.slice(2);
    main(args);
}

// Export the main function so it can be called from the tests
module.exports.main = main
