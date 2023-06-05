import {
    createAgent,
    enableCameraControlls,
    moveAgent,
    loadAnimations,
    updateAgent,
} from './mechanics/index.js'

const WIDTH = 800
const HEIGHT = 500

class GameScene extends Phaser.Scene {
    constructor() {
        super({ key: 'GameScene' })
    }

    preload() {
        this.load.tilemapTiledJSON('my_map', 'assets/map.json')
        this.load.image('dialog', 'assets/dailog.png')
        this.load.image('my_tileset', 'assets/sprites.png')
        this.load.spritesheet('player', 'assets/Adam_16x16.png', {
            frameWidth: 16,
            frameHeight: 32,
        })
    }

    create() {
        function loadTilemap(scene) {
            const map = scene.make.tilemap({ key: 'my_map' })
            const tileset = map.addTilesetImage('my_tileset')
            const ground = map.createLayer('ground', tileset)

            map.createLayer('upperground', tileset)
            map.createLayer('structs2', tileset)
            map.createLayer('structs', tileset)
            const wallLayer = map.createLayer('wall', tileset)
            const objectLayer = map.getObjectLayer('walls')
            wallLayer.setCollisionByProperty({ collides: true })

            scene.navMesh = scene.navMeshPlugin.buildMeshFromTiled(
                'mesh',
                objectLayer,
                [wallLayer]
            )

            scene.navMesh.debugDrawMesh({
                drawCentroid: true,
                drawBounds: true,
                drawNeighbors: true,
                drawPortals: true,
            })
        }

        enableCameraControlls(game, this)
        loadTilemap(this)
        loadAnimations(this)

        createAgent({
            scene: this,
            name: 'Medina',
            location: 'Forest: Campfire',
            activity: 'Staying warm by the campfire',
            memories: [
                'Loves to cook',
                'Hates to be around campfires',
                'Loves to hang out inside the Green House',
                'Has a friend that lives in the Red House',
            ],
        })
    }
}

class UIScene extends Phaser.Scene {
    constructor() {
        super({ key: 'UIScene', active: true })
    }

    create() {
        function setupButtons(scene) {
            let updateStateButton = scene.add.dom(70, 30).createFromHTML(
                /*html*/
                `<button
                        class="nes-btn is-primary"
                        id="smallville--next"
                    >
                        Update State
                    </button>`
            )
            updateStateButton.setScrollFactor(0, 0)
            scene.add.dom(200, 30).createFromHTML(
                /*html*/
                `<button class="nes-btn is-secondary" id="auto-update">
                        Auto-Update?
                    </button>`
            )

            scene.add.dom(330, 30).createFromHTML(
                /*html*/
                `<div
                        style="display: flex; flex-direction: column; justify-content: center; align-items: center; padding: 0; box-sizing: border-box; height: 50px; width: 70px"
                        class="nes-container is-dark"
                    >
                        <span
                            style="align-items: center; text-align: center;"
                            id="seconds-indicator"
                            class="nes-text"
                        ></span>
                    </div>`
            )

            let moreButton = scene.add.dom(WIDTH - 30, 30).createFromHTML(
                /*html*/
                `<button class="nes-btn is-secondary">⋮</button>`
            )

            
                
            scene.add
                .dom(0, 0)
                .createFromHTML(
                    /*html*/
                    `<div id="settings" class="modal-container display-none" style="width: ${WIDTH}px; height: ${HEIGHT}px">
                        <div class="modal">
                            <div class="modal-header">
                                <h3>Settings</h3>
                                <button id="close-modal">✕</button>
                            </div>
                            <div class="modal-body">
                                Timestep (mins):
                                <input type="number" id="timestep" name="timestep"
                                min="1" value="1">
                                <button 
                                    id="timestep-submit"
                                   
                                >
                                Submit
                                </button>
                            </div>
                        </div>
                    </div>`
                )
                .setOrigin(0)
                .setPosition(0, 0)

            
            document
                .getElementById('timestep-submit')
                .addEventListener('click', (e) => {
                    let timestepToSend = parseInt(document.getElementById('timestep').value)
                    fetch('http://localhost:8080/timestep', {
                        method: 'POST',
                        mode: 'cors', // no-cors, *cors, same-origin
                        cache: 'no-cache', // *default, no-cache, reload, force-cache, only-if-cached
                        headers: {
                            'Content-Type': 'application/json',
                        },
                        body: JSON.stringify({ numOfMinutes: timestepToSend }),
                    })
                })

                let settings = document.getElementById('settings')


                moreButton.node.addEventListener('click', e => {
                    settings.classList.remove('display-none')
                })

            document
                .getElementById('close-modal')
                .addEventListener('click', (e) => {
                    settings.classList.add('display-none')
                })

            return [updateStateButton]
        }

        function setupDebug(scene) {
            let debug = scene.add.dom(500, 0).createFromHTML(
                /*html*/
                `<div id="debug"><p>Mouse Coords <span id="smallville--debug-mouse">(0, 0)</span></p></div>`
            )
            debug.setScrollFactor(0, 0)
            return [debug]
        }

        setupButtons(this)
    }
}

var config = {
    type: Phaser.AUTO,
    physics: {
        default: 'arcade',
        arcade: {
            gravity: { y: 0 },
        },
    },
    scale: {
        width: WIDTH,
        height: HEIGHT,
    },
    plugins: {
        scene: [
            {
                key: 'PhaserNavMeshPlugin',
                plugin: PhaserNavMeshPlugin,
                mapping: 'navMeshPlugin',
                start: true,
            },
        ],
    },
    scene: [GameScene, UIScene],
    parent: 'phaser-container',
    antialias: true,
    dom: {
        createContainer: true,
    },
}

const game = new Phaser.Game(config)

export { moveAgent }
