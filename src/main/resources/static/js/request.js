async function req() {
    const apiKey = $("#apikey").val();
    const res = await fetch("http://localhost:8080/api/v1/user", {
            method: "GET",
            headers: {
                "Content-Type": "application/json",
                "X-API-KEY": apiKey,
            }
        });

    if (res.status != 200) {
        const data = await res.json();
        $("#result").empty();
        $("#result").append(`<p>Error status: ${res.status}</p><p>Error message: ${data.error}</p>`);
    } else {
        const data = await res.json();
        $("#result").empty();
        $("#result").append(`
        <p>Username: <a>${data.username}</a></p>
        <p>Enable: <a>${data.enabled}</a></p>
        <p>Roles: <a>${data.roles}</a></p>
        `);
    }

}

$(function () {
    $("form").on("submit", (e) => e.preventDefault());
    $("#request").click(() => req());
});