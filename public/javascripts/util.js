function scale(sprite3d) {
	var minX = 100;
	var minY = 100;
	var minZ = 100;
	var maxX = -100;
	var maxY = -100;
	var maxZ = -100;
	function _cs(node, px, py, pz) {
		if (node.mesh) {
			for ( var i = 0, end = node.mesh.vertices.length; i < end; i += 3) {
				var x = Number(node.mesh.vertices[i + 0]);
				var y = Number(node.mesh.vertices[i + 1]);
				var z = Number(node.mesh.vertices[i + 2]);
				if (x + px + node.x < minX) {
					minX = x + px + node.x;
				}
				if (y + py + node.y < minY) {
					minY = y + py + node.y;
				}
				if (z + pz + node.z < minZ) {
					minZ = z + pz + node.z;
				}
				if (x + px + node.x > maxX) {
					maxX = x + px + node.x;
				}
				if (y + py + node.y > maxY) {
					maxY = y + py + node.y;
				}
				if (z + pz + node.z > maxZ) {
					maxZ = z + pz + node.z;
				}
			}
		}
		$.each(node.childNodes, function(i, child) {
			_cs(child, px + node.x, py + node.y, pz + node.z);
		});
	}
	_cs(sprite3d, 0, 0, 0);

	var size = (maxY - minY);
	if (size < (maxX - minX) / 2) {
		size = (maxX - minX) / 2;
	}
	if (size < (maxZ - minZ) / 2) {
		size = (maxZ - minZ) / 2;
	}
	var sc = 1.0 / size;
	if (size) {
		sprite3d.scale(sc, sc, sc);
		// sprite3d.y = -(minY * sc) + 0.1;
	}
}
