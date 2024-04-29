#region Copyright
/*************************************************************************/
/*  GooglePlayBillingConfig.cs                                           */
/*************************************************************************/
/*                       This file is part of:                           */
/*                           GODOT ENGINE                                */
/*                      https://godotengine.org                          */
/*************************************************************************/
/* Copyright (c) 2007-2020 Juan Linietsky, Ariel Manzur.                 */
/* Copyright (c) 2014-2020 Godot Engine contributors (cf. AUTHORS.md).   */
/*                                                                       */
/* Permission is hereby granted, free of charge, to any person obtaining */
/* a copy of this software and associated documentation files (the       */
/* "Software"), to deal in the Software without restriction, including   */
/* without limitation the rights to use, copy, modify, merge, publish,   */
/* distribute, sublicense, and/or sell copies of the Software, and to    */
/* permit persons to whom the Software is furnished to do so, subject to */
/* the following conditions:                                             */
/*                                                                       */
/* The above copyright notice and this permission notice shall be        */
/* included in all copies or substantial portions of the Software.       */
/*                                                                       */
/* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,       */
/* EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF    */
/* MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.*/
/* IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY  */
/* CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,  */
/* TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE     */
/* SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.                */
/*************************************************************************/
#endregion

#if TOOLS

using System;
using Godot;

namespace GooglePlayBilling;

[Tool]
public partial class GooglePlayBillingConfig : EditorExportPlugin
{
    private const string PLUGIN_NAME = "GodotGooglePlayBilling";

    // Dependency paths relative to the project's addons folder.
    private const string LIB_PATH_RELEASE = "google_play_billing/libs/GodotGooglePlayBilling.1.2.0.release.aar";
    private const string LIB_PATH_DEBUG = "google_play_billing/libs/GodotGooglePlayBilling.1.2.0.debug.aar";
    private const string BILLING_DEPENDENCY = "com.android.billingclient:billing:5.2.1";


    public override bool _SupportsPlatform(EditorExportPlatform platform)
    {
        if (platform is EditorExportPlatformAndroid)
        {
            return true;
        }
        else
        {
            return false;
        }
    }
    public override string[] _GetAndroidLibraries(EditorExportPlatform platform, bool debug)
    {
        if (debug)
        {
            return new string[] { LIB_PATH_DEBUG };
        }
        else
        {
            return new string[] { LIB_PATH_RELEASE };
        }
    }
    public override string[] _GetAndroidDependencies(EditorExportPlatform platform, bool debug)
    {
        return new string[] { BILLING_DEPENDENCY };
    }
    public override string _GetName()
    {
        return PLUGIN_NAME;
    }
}

#endif