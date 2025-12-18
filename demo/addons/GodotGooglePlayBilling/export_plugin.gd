@tool
extends EditorPlugin

# A class member to hold the editor export plugin during its lifecycle.
var export_plugin : BillingPluginExportPlugin

func _enter_tree():
	# Initialization of the plugin goes here.
	export_plugin = BillingPluginExportPlugin.new()
	add_export_plugin(export_plugin)


func _exit_tree():
	# Clean-up of the plugin goes here.
	remove_export_plugin(export_plugin)
	export_plugin = null


class BillingPluginExportPlugin extends EditorExportPlugin:
	var _plugin_name = "GodotGooglePlayBilling"

	func _supports_platform(platform):
		if platform is EditorExportPlatformAndroid:
			return true
		return false

	func _get_android_libraries(platform, debug):
		if debug:
			return PackedStringArray([_plugin_name + "/bin/debug/" + _plugin_name + "-debug.aar"])
		else:
			return PackedStringArray([_plugin_name + "/bin/release/" + _plugin_name + "-release.aar"])

	func _get_android_dependencies(platform, debug):
		if debug:
			return PackedStringArray(["com.android.billingclient:billing-ktx:8.0.0"])
		else:
			return PackedStringArray(["com.android.billingclient:billing-ktx:8.0.0"])

	func _get_name():
		return _plugin_name
