enchant();

$(function() {
	var game = new Game(320, 320);
	game.preload(modelUrl);
	game.onload = function() {
		var scene = new Scene3D();
		scene.backgroundColor = "#004488";

		var camera = new Camera3D();
		camera.y = 0;
		camera.z = 6;
		scene.setCamera(camera);

		var model = game.assets[modelUrl];
		scale(model);

		var modelWrapper = new Sprite3D();
		modelWrapper.addChild(model);
		scene.addChild(modelWrapper);
		modelWrapper.addEventListener("enterframe", function(e) {
			this.rotationApply(new Quat(0, 1, 0, 0.01));
		});

		var startPos = null;
		modelWrapper.addEventListener("touchstart", function(e) {
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
		modelWrapper.addEventListener("touchmove", function(e) {
			this.x = this.lastPos.x + (e.x - startPos.x) / 160;
			this.y = this.lastPos.y - (e.y - startPos.y) / 160;
		});
		modelWrapper.addEventListener("touchend", function(e) {
			startPos = null;
		});

		if (model.animate) {
			var poses = new Group();
			game.rootScene.addChild(poses);
			Label.prototype.button = function() {
				this._style.width = "100px";
				this._style.padding = "2px";
				this._style.backgroundColor = "rgba(128,255,128,0.3)";
				this._style.fontWeight = "bold";
				this._style.textAlign = "center";
				this._style.cursor = "pointer";
				this._style.fontSize = "9pt";
			};

			var poseNames = [];
			for ( var pose in model.poses) {
				if (model.poses.hasOwnProperty(pose)) {
					poseNames.push(pose);
				}
			}
			poseNames = poseNames.sort();

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

				y += 20;
			});
			model.animate("_initialPose", 1);
		}
	};
	game.start();
});