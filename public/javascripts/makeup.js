enchant();

var game;

$(function() {
	game = new Game(320, 320);
	game.preload("/assets/images/ajax-loader2.gif");
	game.onload = function() {
		var scene = new Scene3D();
		scene.backgroundColor = "#004488";

		var camera = new Camera3D();
		camera.y = 1;
		camera.z = 6;
		scene.setCamera(camera);

		var obj = new Sprite3D();
		obj.addEventListener("enterframe", function(e) {
			if (game.input.up) {
				camera.z += -0.1;
			}
			if (game.input.down) {
				camera.z += 0.1;
			}
			if (game.input.left) {
				this.rotationApply(new Quat(0, 1, 0, 0.1));
			}
			if (game.input.right) {
				this.rotationApply(new Quat(0, 1, 0, -0.1));
			}
		});
		scene.addChild(obj);

		obj.entity = new Sprite3D();
		obj.entityName = null;
		obj.addChild(obj.entity);
		var startPos = null;
		obj.addEventListener("touchstart", function(e) {
			startPos = {
				x : e.x,
				y : e.y
			};
			this.lastPos = {
				x : this.x,
				y : this.y,
				z : this.z
			};
		});
		obj.addEventListener("touchmove", function(e) {
			this.x = this.lastPos.x + (e.x - startPos.x) / 160;
			this.y = this.lastPos.y - (e.y - startPos.y) / 160;
		});
		obj.addEventListener("touchend", function(e) {
			startPos = null;
		});

		var loading = new Sprite(100, 100);
		loading.x = (game.width - loading.width) / 2;
		loading.y = (game.height - loading.height) / 2;
		loading.image = game.assets["/assets/images/ajax-loader2.gif"];
		loading.visible = false;
		game.loading = loading;
		game.rootScene.addChild(loading);

		Label.prototype.button = function() {
			this._style.width = "100px";
			this._style.padding = "2px";
			this._style.backgroundColor = "rgba(128,255,128,0.3)";
			this._style.fontWeight = "bold";
			this._style.textAlign = "center";
			this._style.cursor = "pointer";
			this._style.fontSize = "9pt";
		};

		var poses;
		$(".file-node").click(function(e) {
			if (poses && poses.parentNode) {
				poses.parentNode.removeChild(poses);
			}
			poses = new Group();
			game.rootScene.addChild(poses);

			var data = $(this).attr("data-value");
			$("#selected-file").val(data);
			game.loading.visible = true;
			$("#submit-area").hide();
			game.load("/data/" + data + ".js", function() {
				game.loading.visible = false;

				obj.removeChild(obj.entity);
				{
					obj.entity = undefined;
					game.assets[obj.entityName] = undefined;
					obj.entity = game.assets["/data/" + data + ".js"];
					scale(obj.entity);
					obj.entityName = "/data/" + data + ".js";
				}
				obj.addChild(obj.entity);

				$("#submit-area").show();

				if (obj.entity.animate) {
					var model = obj.entity;
					var poseNames = [];
					for ( var pose in model.poses) {
						if (model.poses.hasOwnProperty(pose)) {
							poseNames.push(pose);
						}
					}
					
					poseNames = poseNames.sort();
					console.log(poseNames);

					var y = 0;
					poseNames.forEach(function(pose) {
						var label = new Label(pose);
						label.button();
						label.poseName = pose;
						label.x = 0;
						label.y = y;
						label.ontouchend = function() {
							model.animate(this.poseName, 20);
						};
						poses.addChild(label);

						y += 24;
					});
					model.animate("_initialPose", 1);
				}
			});
		});
	};
	game.start();
});
