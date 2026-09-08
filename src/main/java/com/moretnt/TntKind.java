package com.moretnt;

/** Every variant has a distinct target, shape, or world-changing utility. */
public enum TntKind {
	LUMBER("lumber_tnt", "伐木 TNT", "只清除原木、木头和树叶。", 6),
	SOIL("soil_tnt", "土方 TNT", "只清除泥土、沙砾、黏土和泥巴。", 6),
	STONE("stone_tnt", "采石 TNT", "只清除常见石头、深板岩和石灰岩。", 6),
	ORE_MINER("ore_miner_tnt", "矿脉 TNT", "只开采矿石；会识别大多数模组矿石。", 5),
	ORE_SAFE("ore_safe_tnt", "护矿 TNT", "破坏非矿物方块，保留原版和模组矿石。", 6),
	CROP("crop_tnt", "收获 TNT", "只收割农作物、花和藤蔓。", 6),
	GLASS("glass_tnt", "玻璃 TNT", "只拆除玻璃、染色玻璃和玻璃板。", 6),
	WOOL("wool_tnt", "羊毛 TNT", "只拆除羊毛、地毯和床。", 6),
	CONCRETE("concrete_tnt", "混凝土 TNT", "只清除混凝土、混凝土粉末和陶瓦。", 6),
	ICE("ice_tnt", "碎冰 TNT", "只清除冰、浮冰和雪。", 6),
	NETHER("nether_tnt", "下界采掘 TNT", "只清除下界岩、玄武岩和黑石。", 6),
	ENDSTONE("endstone_tnt", "末地采掘 TNT", "只清除末地石和紫珀方块。", 6),
	WOOD("wood_tnt", "木材 TNT", "只拆除木制建筑方块，不动矿物。", 6),
	WATER("water_tnt", "注水 TNT", "在爆点周围的空气格灌入水源。", 4),
	LAVA("lava_tnt", "灌岩浆 TNT", "在爆点周围的空气格灌入岩浆。", 2),
	FROST("frost_tnt", "冻结 TNT", "把附近的水源冻结成冰。", 5),
	FIRE("fire_tnt", "燃烧 TNT", "在安全的空位点燃火焰。", 4),
	GLOW("glow_tnt", "照明 TNT", "在附近空位安放萤石照明。", 4),
	SPONGE("sponge_tnt", "排水 TNT", "清除附近的水和岩浆。", 5),
	TUNNEL("tunnel_tnt", "隧道 TNT", "开凿一条南北向的 3×3 长隧道。", 12),
	SHAFT("shaft_tnt", "竖井 TNT", "开凿一条 3×3 的垂直竖井。", 12),
	QUARRY("quarry_tnt", "采石场 TNT", "挖掘石材与矿石；保留箱子等容器。", 7),
	SURFACE("surface_tnt", "平整 TNT", "清除爆点同高度的地表土方。", 8),
	TRENCH("trench_tnt", "壕沟 TNT", "挖出一条东西向的宽壕沟。", 12),
	DEMOLITION("demolition_tnt", "拆除 TNT", "通用拆除，但会保护矿石和带容器的方块。", 6),
	MEGA("mega_tnt", "巨爆 TNT", "半径 16 的大型常规爆炸，请远离建筑。", 16),
	COLOSSAL("colossal_tnt", "超巨爆 TNT", "半径 32 的超大爆炸，仅建议在空旷区使用。", 32),
	OBSIDIAN("obsidian_tnt", "黑曜石 TNT", "专门拆除黑曜石、哭泣的黑曜石和重生锚。", 5),
	BEDROCK("bedrock_tnt", "基岩 TNT", "破坏半径 4 内的基岩；不会掉落基岩。", 4),
	VOID("void_tnt", "清空 TNT", "清除方块且不掉落物品，保留矿石与容器。", 7);

	private final String id;
	private final String title;
	private final String description;
	private final int radius;

	TntKind(String id, String title, String description, int radius) {
		this.id = id;
		this.title = title;
		this.description = description;
		this.radius = radius;
	}

	public String id() {
		return id;
	}

	public String title() {
		return title;
	}

	public String description() {
		return description;
	}

	public int radius() {
		return radius;
	}

	/** Resolves persistent entity data without relying on enum ordinal order. */
	public static TntKind byId(String id) {
		for (TntKind kind : values()) {
			if (kind.id.equals(id)) {
				return kind;
			}
		}
		return LUMBER;
	}
}
