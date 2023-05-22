import { createAgent, enableCameraControlls, moveAgent, loadAnimations } from './mechanics/index.js'

class GameScene extends Phaser.Scene {
    constructor() {
        super({ key: 'GameScene' });
    }

    preload() {
        this.load.tilemapTiledJSON('my_map', 'assets/map.json');
        this.load.image('my_tileset', 'assets/sprites.png');
        this.load.spritesheet('player', 'assets/Adam_16x16.png', {
            frameWidth: 16,
            frameHeight: 32
        });
    }

    create() {
        function loadTilemap(scene) {
            const map = scene.make.tilemap({ key: 'my_map' });
            const tileset = map.addTilesetImage('my_tileset');
            map.createLayer('ground', tileset);
            map.createLayer('upperground', tileset);
            map.createLayer('structs2', tileset);
            map.createLayer('structs', tileset);
        }

        enableCameraControlls(game, this);
        loadTilemap(this);
        loadAnimations(this)

        createAgent({
            scene: this,
            name: "Medina",
            location: "Forest: Campfire",
            activity: "Staying warm by the campfire",
            memories: [
                "Does not know anyone",
                "Loves to cook",
                "Likes to sometimes hang out by campfires"
            ]
        })
    }
}

class UIScene extends Phaser.Scene {
    constructor() {
        super({ key: 'UIScene', active: true });
    }

    create() {
        function setupButtons(scene) {
            let updateStateButton = scene.add.dom(149, 24).createFromHTML('<button class="nes-btn" id="smallville--next">Update State</button>');
            updateStateButton.setScrollFactor(0,0);
            return [updateStateButton]
        }

        function setupDebug(scene){
            let debug = scene.add.dom(500, 0).createFromHTML('<div id="debug"><p>Mouse Coords <span id="smallville--debug-mouse">(0, 0)</span></p></div>');
            debug.setScrollFactor(0,0);
            return [debug]
        }

        setupDebug(this)
        setupButtons(this)
    }
}

var config = {
    type: Phaser.AUTO,
    physics: {
        default: 'arcade',
        arcade: {
            gravity: { y: 200 }
        }
    },
    scale: {
        width: 800,
        height: 500,
    },
    scene: [GameScene, UIScene],
    parent: 'phaser-container',
    dom: {
        createContainer: true
    }
};

const game = new Phaser.Game(config);

export { moveAgent }