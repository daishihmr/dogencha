enchant();

var game;
$(function() {
	game = new Game(440, 360);

	var loadData = [].concat($.map(modelNames, function(m) {
		return "/data/" + m + ".js";
	}));
	loadData.push("http://static.dev7.jp/enchant.js/images/font2.png");
	game.preload(loadData);

	game.onload = function() {
		var scene = new Scene3D();
		scene.backgroundColor = "#004488";

		var camera = new Camera3D();
		camera.y = 0;
		camera.z = 6;
		scene.setCamera(camera);
		game.addEventListener("enterframe", function() {
			if (game.input.up) {
				camera.y -= 0.05;
				camera.centerY -= 0.025;
			}
			if (game.input.down) {
				camera.y += 0.05;
				camera.centerY += 0.025;
			}
		});

		for ( var i = 0; i < 10; i++) {
			var f = game.assets["/data/" + modelNames[i] + ".js"];
			if (f) {
				var pos = new Sprite3D();
				pos.addChild(f);
				pos.x = i * 5;
				scale(pos);
				scene.addChild(pos);
				pos.addEventListener("enterframe", function(e) {
					this.rotateYaw(0.01);
				});

				if (f.poses) {
					f.curPose = 0;
					f.addEventListener("enterframe", function(e) {
						var poseNames = $.map(this.poses, function(v, k) {
							return k;
						})
						poseNames = poseNames.sort();
						if (this.age % 90 == 0) {
							var pose = poseNames[this.curPose];
							this.animate(pose, 30);
							this.curPose = (this.curPose + 1)
									% poseNames.length;
						}
					});
				}
			}
		}

		var cursor = 0;
		var cv = 0;
		game.addEventListener("enterframe", function(e) {
			if (Math.abs(cursor * 5 - camera.x) > 0.01) {
				camera.x += (cursor * 5 - camera.x) / 4;
				camera.centerX += (cursor * 5 - camera.centerX) / 4;
			}
		});

		var toLeft = function() {
			if (cursor > 0) {
				cursor -= 1;
			}
			if (cursor == 0) {
				left.visible = false;
			} else {
				left.visible = true;
			}
			if (cursor == 9) {
				right.visible = false;
			} else {
				right.visible = true;
			}

			showDataDetail(cursor);
		};
		var toRight = function() {
			if (cursor < 9) {
				cursor += 1;
			}
			if (cursor == 0) {
				left.visible = false;
			} else {
				left.visible = true;
			}
			if (cursor == 9) {
				right.visible = false;
			} else {
				right.visible = true;
			}

			showDataDetail(cursor);
		};
		game.addEventListener("leftbuttonup", toLeft);
		game.addEventListener("rightbuttonup", toRight);

		var left = new Sprite(16, 16);
		left.image = game.assets["/assets/enchant.js/images/font2.png"];
		left.frame = 28;
		left.scale(2, 2);
		left.moveTo(8, game.height - left.height - 8);
		left.addEventListener("touchend", toLeft);
		left.visible = false;
		game.rootScene.addChild(left);

		var right = new Sprite(16, 16);
		right.image = game.assets["/assets/enchant.js/images/font2.png"];
		right.frame = 30;
		right.scale(2, 2);
		right.moveTo(game.width - right.width - 8, game.height - right.height
				- 8);
		right.addEventListener("touchend", toRight);
		game.rootScene.addChild(right);

		showDataDetail(0);
	};
	game.start();
});

function createShadow(s) {
	var tex = new Texture();
	tex.ambient = [ 0, 0, 0, 1 ];
	tex.diffuse = [ 0, 0, 0, 1 ];
	tex.specular = [ 0, 0, 0, 1 ];
	tex.shininess = 90;
	function _cs(node) {
		var result = new Sprite3D();
		if (node.mesh) {
			var m = new Mesh();
			m.vertices = $.map(node.mesh.vertices, function(v, i) {
				if (i % 3 == 1) {
					return 0;
				} else {
					return v;
				}
			});
			m.normals = m.vertices;
			m.texCoords = node.mesh.texCoords;
			m.indices = node.mesh.indices;
			m.setBaseColor("#000000");
			m.texture = tex;
			result.mesh = m;
		}
		$.each(node.childNodes, function(i, child) {
			result.addChild(_cs(child));
		});
		return result;
	}
	return _cs(s);
}

function showDataDetail(cursor) {
	var modelName = modelNames[cursor];
	console.log(modelName);
	setDetailHref(modelName)
}
