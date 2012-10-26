if (enchant.gl != undefined) {
	enchant.Game.prototype._original_load = enchant.Game.prototype.load;
	enchant.Game.prototype.load = function(src, callback) {
		if (typeof (callback) !== "function") {
			callback = function() {
			};
		}
		var lower = src.toLowerCase();
		if (lower.match(/\.suf\.js$/) || lower.match(/\.suf$/)) {
			enchant.gl.Sprite3D.loadSuf(src, function(suf) {
				enchant.Game.instance.assets[suf.url] = suf;
				callback();
			});
		} else if (lower.match(/\.l3p\.js$/) || lower.match(/\.l3p$/)) {
			enchant.gl.Sprite3D.loadL3p(src, function(l3p) {
				enchant.Game.instance.assets[l3p.url] = l3p;
				callback();
			});
		} else if (lower.match(/\.l3c\.js$/) || lower.match(/\.l3c$/)) {
			enchant.gl.Sprite3D.loadL3c(src, function(l3c) {
				enchant.Game.instance.assets[l3c.url] = l3c;
				callback();
			});
		} else {
			this._original_load(src, callback);
		}
	};
	(function() {
		enchant.gl.Sprite3D.loadSuf = function(url, onload) {
			if (typeof onload !== "function") {
				throw new Error("Argument must be function");
				return null;
			}
			var ajaxParam = {
				url : url,
				dataType : (url.match(/http/) == null) ? "json" : "jsonp",
				jsonp : "callback",
				success : function(json) {
					// console.log("suf ajax success", url, json);

					// build
					var suf = new Sprite3D();
					$.each(json, function(i, mesh) {
						// console.debug(mesh);
						var part = new Sprite3D();

						part.mesh = new Mesh();
						part.mesh.vertices = mesh.vertices;
						part.mesh.normals = mesh.normals;
						part.mesh.texCoords = mesh.texCoords;
						part.mesh.indices = mesh.indices;
						part.mesh.setBaseColor("#ffffffff");

						var tex = new Texture();
						if (mesh.texture.src) {
							// tex.src = mesh.texture.src;
						}
						tex.ambient = mesh.texture.ambient;
						tex.diffuse = mesh.texture.diffuse;
						tex.specular = mesh.texture.specular;
						tex.emission = mesh.texture.emission;
						tex.shininess = mesh.texture.shininess;
						part.mesh.texture = tex;

						suf.addChild(part);
					});

					suf.url = url;
					onload(suf);
				},
				complete : function() {
					// console.log("suf ajax complete", url);
				}
			};
			// console.log("suf ajax(" + ajaxParam.dataType + ") start", url)
			$.ajax(ajaxParam);
		};

		function buildL3p(objects) {
			var root = new Sprite3D();
			$.each(objects, function(atrName, data) {
				var part = new Sprite3D();

				var part = new Sprite3D();
				var mesh = new Mesh();
				mesh.vertices = data.vertices;
				mesh.normals = data.normals;
				mesh.texCoords = data.texCoords;
				mesh.indices = data.indices;
				mesh.setBaseColor("#ffffffff");

				var tex = new Texture();
				if (data.texture.src) {
					tex.src = data.texture.src;
				}
				tex.ambient = data.texture.ambient;
				tex.diffuse = data.texture.diffuse;
				tex.specular = data.texture.specular;
				tex.emission = data.texture.emission;
				tex.shininess = data.texture.shininess;

				part.mesh = mesh;
				part.mesh.texture = tex;

				part.name = data.name;

				root.addChild(part);
			});
			return root;
		}
		enchant.gl.Sprite3D.loadL3p = function(url, onload) {
			var _this = this;
			if (typeof onload != "function") {
				throw new Error("Argument must be function");
				return null;
			} else {
				_this.onload = onload;
			}

			var ajaxParam = {
				url : url,
				dataType : (url.match(/http/) == null) ? "json" : "jsonp",
				jsonp : "callback",
				success : function(json, status) {
					// console.log("l3p ajax success", url, json);
					var l3p = buildL3p(json);
					l3p.url = url;
					onload(l3p);
				}
			};
			// console.log("l3p ajax(" + ajaxParam.dataType + ") start", url);
			$.ajax(ajaxParam);
		};

		/** @constructor */
		function L3c() {
			var _this = this;
			this.loadModel = function(url) {
				// console.log("l3c load ajax start", url);
				var ajaxParam = {
					url : url,
					dataType : (url.match(/http/) == null) ? "json" : "jsonp",
					jsonp : "callback",
					success : function(data, status) {
						// console.log("l3c load ajax success", url, data);
						_this.onload(data);
					}
				};
				$.ajax(ajaxParam);
			};
			this.build = function(data) {
				function _tree(_unit) {
					var _result = {};
					_result.node = buildL3p(_unit.l3p);
					_result.basePosition = _unit.basePosition;
					if (_unit.childUnits instanceof Array) {
						_result.child = $.map(_unit.childUnits, function(c) {
							return _tree(c);
						});
					}
					return _result;
				}
				function _setupPoseNode(poseNode) {
					poseNode.quat = Utils.newQuat(poseNode.quat);
					$.each(poseNode.childUnits, function(i, v) {
						v.parentNode = poseNode;
						_setupPoseNode(v);
					});
				}

				var result = Unit.build(_tree(data.root));

				result.poses = {};
				$.each(data.poses, function(k, v) {
					result.poses[v.name] = v.root;
					_setupPoseNode(result.poses[v.name]);
				});

				if (result.poses._initialPose) {
					result.setPose(result.poses._initialPose);
				}
				return result;
			};
		}
		enchant.gl.Sprite3D.loadL3c = function(_url, onload) {
			var _this = this;
			if (typeof onload != "function") {
				throw new Error("Argument must be function");
				return null;
			} else {
				_this.onload = onload;
			}

			var l3c = new L3c();
			l3c.onload = function(objects) {
				var d = this.build(objects);
				d.url = _url;
				_this.onload(d);
			}
			l3c.loadModel(_url);
		};
	})();
}

/**
 * DoGA L3Cモデルのユニット.
 */
var Unit = enchant.Class.create(enchant.gl.Sprite3D, {
	initialize : function(entity, basePosition) {
		enchant.gl.Sprite3D.call(this);

		/**
		 * データの実体
		 * 
		 * @type {Sprite3D}
		 */
		this._entity = entity;

		/**
		 * ユニットの基本位置.
		 */
		this.baseX = basePosition[0];
		this.baseY = basePosition[1];
		this.baseZ = basePosition[2];

		/**
		 * ユニットツリーの子ノード.
		 * 
		 * enchant.jsのイベントツリーとは関係ない.
		 */
		this.childUnits = [];

		/**
		 * アニメーション中に毎フレーム実行される関数.
		 */
		this.tickListener = null;

		/** ポーズデータ.ルートオブジェクトのみが持つ. */
		this.poses = null;

		this.addChild(entity);
		this.addEventListener("enterframe", (function() {
			var listener = function() {
				if (this.tickListener) {
					this.tickListener();
				}
			};
			listener.animation = true;
			return listener;
		})());
	},
	entity : {
		get : function() {
			return this._entity;
		}
	},
	/**
	 * 子ユニットを追加する.
	 */
	addChildUnit : function(childUnit) {
		this.addChild(childUnit);
		this.childUnits[this.childUnits.length] = childUnit;
	},
	animate : function(pose, frameNum, callback) {
		if (typeof (pose) == "string") {
			pose = this.poses[pose];
		}
		if (!pose) {
			pose = this.poses._initialPose;
		}
		this.isAnimating = true;
		var startFrame = this.age;
		var endFrame = this.age + frameNum;

		var from = this.getPose();

		this.tickListener = function(e) {
			// この内部のthisはUnitを指す
			this.setFrame(from, pose, (this.age - startFrame) / frameNum);
			if (this.age >= endFrame) {
				this.isAnimating = false;
				this.setPose(pose);
				this.tickListener = null;
				if (callback) {
					callback.apply(this);
				}
			}
		};
	},
	cancelAnimation : function() {
		this.tickListener = null;
	},
	setFrame : function(from, to, rate) {
		if (!from) {
			from = this.poses._initialPose;
		}
		if (!to) {
			to = this.poses._initialPose;
		}
		function _setFrameToUnit(node, fromNode, toNode, rate) {
			if (!fromNode) {
				fromNode = {};
				fromNode.pose = [ 0, 0, 0, 0, 0, 0 ];
				fromNode.quat = new Quat(0, 0, 0, 0);
				fromNode.childUnits = [];
			}
			if (!toNode) {
				toNode = {};
				toNode.pose = [ 0, 0, 0, 0, 0, 0 ];
				toNode.quat = new Quat(0, 0, 0, 0);
				toNode.childUnits = [];
			}

			var q = Utils.copyQuat(fromNode.quat);
			var toQ = Utils.copyQuat(toNode.quat);

			q.slerpApply(toQ, rate);
			node.quat = q;
			node.rotationSet(q);

			node.x = fromNode.pose[3] + (toNode.pose[3] - fromNode.pose[3])
					* rate + node.baseX;
			node.y = fromNode.pose[4] + (toNode.pose[4] - fromNode.pose[4])
					* rate + node.baseY;
			node.z = fromNode.pose[5] + (toNode.pose[5] - fromNode.pose[5])
					* rate + node.baseZ;

			$.each(node.childUnits, function(i, child) {
				_setFrameToUnit(child, fromNode.childUnits[i],
						toNode.childUnits[i], rate);
			});
		}
		_setFrameToUnit(this, from, to, rate);
	},
	setPose : function(pose) {
		if (!pose) {
			pose = this.poses._initialPose;
		}
		function _setPoseToUnit(node, p) {
			if (!p) {
				p = {};
				p.pose = [ 0, 0, 0, 0, 0, 0 ];
				p.childUnits = [];
				p.quat = new Quat(0, 0, 0, 0);
			}

			node.quat = p.quat;
			node.rotationSet(p.quat);

			node.x = p.pose[3] + node.baseX;
			node.y = p.pose[4] + node.baseY;
			node.z = p.pose[5] + node.baseZ;

			$.each(node.childUnits, function(i, child) {
				_setPoseToUnit(node.childUnits[i], p.childUnits[i]);
			});
		}
		_setPoseToUnit(this, pose);
	},
	getPose : function() {
		function _getPose(node, parent) {
			var p = {};
			p.pose = [ 0, 0, 0, node.x - node.baseX, node.y - node.baseY,
					node.z - node.baseZ ];
			p.quat = node.quat;
			p.parentNode = parent;
			p.childUnits = [];
			$.each(node.childUnits, function(i, child) {
				p.childUnits[i] = _getPose(node.childUnits[i], p);
			});
			return p;
		}

		return _getPose(this, null);
	},
	clone : function() {
		var clone = new Unit(this._entity.clone(), [ this.baseX, this.baseY,
				this.baseZ ]);
		for ( var i = 0, end = this.childUnits.length; i < end; i++) {
			clone.addChildUnit(this.childUnits[i].clone());
		}
		if (this.poses) {
			clone.poses = this.poses;
			clone.setPose(clone.poses._initialPose);
		}
		return clone;
	}
});

Unit.prototype.quat = new enchant.gl.Quat(0, 0, 0, 0);
Unit.prototype.baseX = 0;
Unit.prototype.baseY = 0;
Unit.prototype.baseZ = 0;

Unit.build = function(arg) {
	var node = new Unit(arg.node, arg.basePosition);
	var child = arg.child;
	if (child instanceof Array) {
		for ( var i = 0, end = child.length; i < end; i++) {
			node.addChildUnit(Unit.build(child[i]));
		}
	}

	return node;
};

var Utils = {};
Utils.newQuat = function(xyzw) {
	var result = new Quat(0, 0, 0, 0);
	result._quat = quat4.create(xyzw);
	return result;
};
Utils.copyQuat = function(quat) {
	var result = new Quat(0, 0, 0, 0);
	result._quat = quat4.create(quat._quat);
	return result;
}