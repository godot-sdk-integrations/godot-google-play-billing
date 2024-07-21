#************************************************************************/
#  GooglePlayBillingEditorPlugin.gd                                     */
#************************************************************************/
#                       This file is part of:                           */
#                           GODOT ENGINE                                */
#                      https://godotengine.org                          */
#************************************************************************/
# Copyright (c) 2007-2020 Juan Linietsky, Ariel Manzur.                 */
# Copyright (c) 2014-2020 Godot Engine contributors (cf. AUTHORS.md).   */
#                                                                       */
# Permission is hereby granted, free of charge, to any person obtaining */
# a copy of this software and associated documentation files (the       */
# "Software"), to deal in the Software without restriction, including   */
# without limitation the rights to use, copy, modify, merge, publish,   */
# distribute, sublicense, and/or sell copies of the Software, and to    */
# permit persons to whom the Software is furnished to do so, subject to */
# the following conditions:                                             */
#                                                                       */
# The above copyright notice and this permission notice shall be        */
# included in all copies or substantial portions of the Software.       */
#                                                                       */
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,       */
# EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF    */
# MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.*/
# IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY  */
# CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,  */
# TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE     */
# SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.                */
#************************************************************************/

@tool
extends EditorExportPlugin

var _plugin_name = "GodotGooglePlayBilling"

# Dependency paths relative to the project's addons folder.
var _lib_path_release = "google_play_billing/libs/GodotGooglePlayBilling.3.0.0.release.aar"
var _lib_path_debug = "google_play_billing/libs/GodotGooglePlayBilling.3.0.0.debug.aar"
var _billing_dependency = "com.android.billingclient:billing:7.0.0"

func _supports_platform(platform):
    if (platform is EditorExportPlatformAndroid):
        return true
    else:
        return false

func _get_android_libraries(platform, debug):
    if (debug):
        return PackedStringArray([_lib_path_debug])
    else:
        return PackedStringArray([_lib_path_release])

func _get_android_dependencies(platform, debug):
    return PackedStringArray([_billing_dependency])
    
func _get_name():
    return _plugin_name